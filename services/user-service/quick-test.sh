#!/bin/bash

# Quick test script for bank statement analysis
# Usage: ./quick-test.sh <path-to-pdf>

if [ $# -eq 0 ]; then
    echo "Usage: ./quick-test.sh <path-to-pdf>"
    echo "Example: ./quick-test.sh '/path/to/bank-statement.pdf'"
    exit 1
fi

PDF_PATH="$1"



echo "Running bank statement analysis..."
echo ""

mvn exec:java -Dexec.mainClass="com.stockfellow.userservice.test.Main" \
    -Dexec.args="'$PDF_PATH'" \
    -Dexec.cleanupDaemonThreads=false \
    -q

echo ""
