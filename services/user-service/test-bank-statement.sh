#!/bin/bash

# Bank Statement Tier Test Script
# Usage: ./test-bank-statement.sh <path-to-pdf>

if [ $# -eq 0 ]; then
    echo "Usage: ./test-bank-statement.sh <path-to-pdf>"
    echo "Example: ./test-bank-statement.sh '/path/to/bank-statement.pdf'"
    exit 1
fi

PDF_PATH="$1"

if [ ! -f "$PDF_PATH" ]; then
    echo "Error: PDF file not found: $PDF_PATH"
    exit 1
fi

echo "Building project..."
cd "$(dirname "$0")"

# Compile the project (skip tests for speed)
mvn clean compile -DskipTests -q

if [ $? -ne 0 ]; then
    echo "Error: Maven compilation failed"
    exit 1
fi

echo "Running bank statement analysis..."
echo ""

# Run the test with Maven
mvn exec:java -Dexec.mainClass="com.stockfellow.userservice.test.Main" \
    -Dexec.args="'$PDF_PATH'" \
    -Dexec.cleanupDaemonThreads=false \
    -q

echo ""
echo "Test completed!"
