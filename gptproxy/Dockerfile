# Start from the official Go image for the build stage
FROM golang:1.20-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Go modules files
COPY ./gptproxy/go.mod ./gptproxy/go.sum ./

# Download dependencies
RUN go mod download

# Copy the source code
COPY ./gptproxy/main.go ./

# Build the Go app
RUN go build -o app main.go

# Start a new stage from a minimal image
FROM alpine:latest

# Set the working directory inside the container
WORKDIR /root/

# Copy the built binary from the builder stage
COPY --from=builder /app/app ./

# Expose port 8084
EXPOSE 8084

# Command to run the executable
CMD ["./app"]
