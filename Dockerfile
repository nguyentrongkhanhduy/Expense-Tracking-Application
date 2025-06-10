# Use a Node.js version compatible with firebase-tools
FROM node:18.17.0-alpine

# Set working directory inside the container
WORKDIR /app

# Copy all action files into the container
COPY . /app

# Install required tools and Firebase CLI
RUN apk update \
  && apk add --no-cache bash git g++ make python3 \
  && yarn global add firebase-tools

# Define the entrypoint script to run this action
ENTRYPOINT ["/app/entrypoint.sh"]