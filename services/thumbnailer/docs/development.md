# Development Setup

## Setting Up the Environment
Copy the `template.env` file to `.env` and open it for editing:
```bash
cp template.env .env && vim .env
# Enter appropriate values for environment variables.

# Install Go dependencies
go mod tidy
```

## Running the Project
To run the project in development mode, use the following command:
```bash
go run ./cmd/thumbnailer
```

This will start the Thumbnailer service and connect it to the RabbitMQ server
specified in your `.env` file.

## Notes
- Ensure RabbitMQ is running and accessible before starting the service.
- Logs will be printed to the console for debugging purposes.