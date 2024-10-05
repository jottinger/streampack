# Start from the official Go image for the build stage
FROM golang:1.23-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Go modules files
COPY go.mod ./

# Download dependencies
RUN go mod download

# Copy the source code
COPY main.go ./

# Build the Go app
RUN go build -o app main.go

# Start a new stage from scratch
FROM alpine:latest

# Set the working directory inside the container
WORKDIR /root/

# Copy the built binary from the builder stage
COPY --from=builder /app/app ./

# Copy the .env file
COPY ./.env ./

# Expose port 8080
EXPOSE 8080

# Command to run the executable
CMD ["./app"]
