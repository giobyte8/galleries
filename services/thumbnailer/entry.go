package main

import (
	"fmt"
	"log"
	"os"

	"github.com/joho/godotenv"
)


func handleErr(msg string, err error) {
	if err != nil {
		log.Panicf("%s: %s", msg, err)
	}
}

func main() {
	fmt.Println("Hello, World!")

	err := godotenv.Load(".env")
	handleErr("Error loading .env file", err)

	rb_host := os.Getenv("RABBITMQ_HOST")
	fmt.Println("RABBITMQ_HOST:", rb_host)
}
