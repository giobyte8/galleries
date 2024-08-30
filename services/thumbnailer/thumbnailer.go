package main

import (
	"fmt"
	"log"
	"os"

	"github.com/joho/godotenv"
	amqp "github.com/rabbitmq/amqp091-go"
)

func main() {
	err := godotenv.Load()
	handleErr("Error loading .env file", err)

	consumeFilesDiscoveryEvents()
}

func handleErr(msg string, err error) {
	if err != nil {
		log.Panicf("%s: %s", msg, err)
	}
}

func assembleAmqpUri() string {
	rb_host := os.Getenv("RABBITMQ_HOST")
	rb_port := os.Getenv("RABBITMQ_PORT")
	rb_user := os.Getenv("RABBITMQ_USER")
	rb_pass := os.Getenv("RABBITMQ_PASS")

	return fmt.Sprintf(
		"amqp://%s:%s@%s:%s/",
		rb_user,
		rb_pass,
		rb_host,
		rb_port)
}

func consumeFilesDiscoveryEvents() {
	exchange := os.Getenv("AMQP_X_GALLERIES")
	qn_discovered_files := os.Getenv("AMQP_Q_DISCOVERED_FILES")

	conn, err := amqp.Dial(assembleAmqpUri())
	handleErr("AMQP - Connection to broker failed", err)
	defer conn.Close()

	ch, err := conn.Channel()
	handleErr("AMQP - Failed to open channel", err)
	defer ch.Close()

	err = ch.ExchangeDeclare(
		exchange,
		"direct",
		true,
		false,
		false,
		false,
		nil)
	handleErr("AMQP - Failed to declare exchange", err)

	q_discovered_files, err := ch.QueueDeclare(
		qn_discovered_files,
		true,
		false,
		false,
		false,
		nil)
	handleErr("AMQP - Failed to declare queue", err)

	err = ch.QueueBind(
		qn_discovered_files,
		qn_discovered_files,
		exchange,
		false,
		nil)
	handleErr("AMQP - Failed to bind queue to exchange", err)

	msgs, err := ch.Consume(
		q_discovered_files.Name,
		"thumbnailer",
		true,
		false,
		false,
		false,
		nil,
	)
	handleErr("AMQP - Failed to create queue consumer", err)

	forever := make(chan bool)
	go func() {
		for msg := range msgs {
			fmt.Printf("Message: %s", msg.Body)
		}
	}()

	log.Println("Starting file discovery events consumption")
	<-forever
}
