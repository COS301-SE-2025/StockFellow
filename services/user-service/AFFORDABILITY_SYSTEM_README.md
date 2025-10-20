# StockFellow Affordability Tier System - Enhanced Bank Statement Processing

## Overview

The StockFellow affordability tier system has been significantly enhanced to support **automatic PDF bank statement processing** for all major South African banks. Users no longer need to manually enter transaction data - they can simply upload their bank statement PDFs.

## Key Improvements Made

### 1. ✅ PDF Bank Statement Extraction
- **New Service**: `BankStatementExtractionService` - Automatically extracts transactions from PDF bank statements
- **Multi-Bank Support**: FNB, Standard Bank, ABSA, Nedbank, Capitec, and generic formats
- **Smart Pattern Matching**: Uses bank-specific patterns with fallback detection
- **Quality Analysis**: Provides extraction quality scores and recommendations

### 2. ✅ Enhanced Transaction Classification
- **Improved Income Detection**: Better recognition of salary, freelance, dividend, and other income types
- **Advanced Savings Detection**: Identifies investment contributions, transfers to savings, and retirement funds
- **Better Expense Categorization**: More accurate expense classification and filtering

### 3. ✅ Fixed Tier Assignment Logic
- **Corrected Base Tier Calculation**: Fixed bug where users were incorrectly assigned to Tier 1
- **Enhanced Logging**: Added detailed debugging information for tier determination
- **Improved Financial Scoring**: More accurate income, expense, and savings behavior scoring

### 4. ✅ New API Endpoints
- **PDF Upload**: `POST /api/users/affordability/analyze-pdf` - Upload PDF bank statements
- **JSON Fallback**: `POST /api/users/affordability/analyze` - Manual transaction entry (legacy)

## API Endpoints

### PDF Bank Statement Analysis (Recommended)
```http
POST /api/users/affordability/analyze-pdf
Content-Type: multipart/form-data
Headers: X-User-Id: {user-id}

Form Data:
- bankStatement: PDF file
```

**Response:**
```json
{
  "success": true,
  "message": "Bank statement PDF analyzed successfully",
  "extractionResult": {
    "transactionsExtracted": 127,
    "qualityScore": 85.5,
    "dateRange": {
      "start": "2024-07-01",
      "end": "2024-10-01"
    },
    "warnings": [],
    "recommendations": ["Ensure the PDF is clear and text is not scanned as images"]
  },
  "affordabilityResult": {
    "tier": 3,
    "tierName": "Balanced Savers",
    "confidence": 0.87,
    "contributionRange": {"min": 500, "max": 1000},
    "analysisDetails": {...}
  }
}
```

### Manual Transaction Analysis (Legacy)
```http
POST /api/users/affordability/analyze
Content-Type: application/json
Headers: X-User-Id: {user-id}

Body:
{
  "transactions": [
    {
      "date": "2024-10-01",
      "description": "SALARY PAYMENT",
      "amount": 15000.00,
      "balance": 15000.00
    }
  ]
}
```

## Supported Bank Formats

### 1. FNB (First National Bank)
- **Date Format**: `dd/MM/yyyy`
- **Pattern**: Date, Description, Amount, Balance
- **Example**: `01/10/2024    SALARY PAYMENT    +15000.00    15000.00`

### 2. Standard Bank
- **Date Format**: `dd-MM-yyyy`
- **Pattern**: Date, Description, Amount, Balance
- **Example**: `01-10-2024    Monthly Salary Transfer    R15000.00    R15000.00`

### 3. ABSA
- **Date Format**: `dd/MM/yyyy` or `yyyy/MM/dd`
- **Pattern**: Date, Description, Amount, Balance
- **Example**: `01/10/2024    SALARY DEPOSIT    R15000.00    R15000.00`

### 4. Nedbank
- **Date Format**: `yyyy-MM-dd`
- **Pattern**: Date, Description, Amount, Balance
- **Example**: `2024-10-01    SALARY PAYMENT    R15000.00    R15000.00`

### 5. Capitec
- **Date Format**: `dd/MM/yyyy` or `yyyy/MM/dd`
- **Pattern**: Date, Description, Amount, Balance
- **Example**: `2024/10/01    SALARY DEPOSIT    R 15,000.00    R 15,000.00`

### 6. Generic/Fallback
- Supports various date formats and layouts
- Automatic pattern detection for unknown banks

## Affordability Tier System

### Tier Structure (Unchanged)
1. **Tier 1 - Essential Savers**: R2,000-R8,000 income → R50-R200 contributions
2. **Tier 2 - Steady Builders**: R8,000-R15,000 income → R200-R500 contributions
3. **Tier 3 - Balanced Savers**: R15,000-R25,000 income → R500-R1,000 contributions
4. **Tier 4 - Growth Investors**: R25,000-R50,000 income → R1,000-R2,500 contributions
5. **Tier 5 - Premium Accumulators**: R50,000-R100,000 income → R2,500-R5,000 contributions
6. **Tier 6 - Elite Circle**: R100,000+ income → R5,000-R10,000 contributions

### Enhanced Analysis Factors

#### Income Analysis
- **Salary Recognition**: Detects salary, wages, bonuses, commissions
- **Freelance Income**: Identifies consulting and contractor payments
- **Investment Income**: Recognizes dividends, interest, rental income
- **Government Benefits**: Detects grants, pensions, allowances

#### Expense Management
- **Spending Patterns**: Analyzes expense-to-income ratios
- **Financial Discipline**: Detects overdrafts, gambling, high-risk spending
- **Regular Commitments**: Identifies insurance, loan payments, subscriptions

#### Savings Behavior
- **Investment Contributions**: Detects unit trusts, retirement funds, ETFs
- **Savings Transfers**: Identifies transfers to savings and investment accounts
- **Emergency Fund**: Analyzes account balance stability

## Error Handling

### Common Issues and Solutions

#### 1. Insufficient Transactions
- **Issue**: Less than 50 transactions extracted
- **Solution**: Upload statement covering 3+ months
- **Response**: 400 Bad Request with recommendation

#### 2. PDF Extraction Failed
- **Issue**: Cannot read PDF or no transactions found
- **Solutions**:
  - Ensure PDF is text-based (not scanned image)
  - Use official bank statement (not screenshot)
  - Check PDF is not password protected
- **Response**: 400 Bad Request with detailed error

#### 3. Unrecognized Bank Format
- **Issue**: Bank format not in supported list
- **Solution**: System automatically tries fallback patterns
- **Fallback**: Generic pattern matching with quality warnings

## Quality Assurance

### Extraction Quality Scoring
- **30 points**: Having transactions extracted
- **25 points**: Sufficient transaction count (50+)
- **25 points**: Adequate date range (90+ days)
- **20 points**: Transaction variety and uniqueness

### Validation Checks
- ✅ Date validation across multiple formats
- ✅ Amount parsing with currency symbols and formatting
- ✅ Duplicate transaction removal
- ✅ Invalid transaction filtering
- ✅ Balance consistency checks

## Testing

### Unit Tests
- Bank-specific format extraction
- Amount parsing with various formats
- Date parsing across different patterns
- Quality analysis calculations
- Error handling scenarios

### Integration Tests
- End-to-end PDF upload and analysis
- Multi-bank format support
- Large file handling
- Performance benchmarks

## Usage Examples

### Frontend Implementation
```typescript
// Upload PDF bank statement
const formData = new FormData();
formData.append('bankStatement', pdfFile);

const response = await fetch('/api/users/affordability/analyze-pdf', {
  method: 'POST',
  headers: {
    'X-User-Id': userId
  },
  body: formData
});

const result = await response.json();
if (result.success) {
  console.log(`User assigned to ${result.affordabilityResult.tierName}`);
  console.log(`Recommended contribution: R${result.affordabilityResult.contributionRange.min} - R${result.affordabilityResult.contributionRange.max}`);
}
```

### Backend Processing Flow
1. **File Upload**: Receive PDF file via multipart request
2. **Bank Detection**: Analyze content to identify bank type
3. **Pattern Matching**: Apply bank-specific extraction patterns
4. **Transaction Parsing**: Extract date, description, amount, balance
5. **Data Cleaning**: Remove duplicates, validate transactions
6. **Financial Analysis**: Calculate income, expenses, savings patterns
7. **Tier Assignment**: Determine affordability tier based on comprehensive scoring
8. **Response**: Return detailed analysis results and recommendations

## Performance Considerations

- **PDF Processing**: Optimized for files up to 10MB
- **Memory Usage**: Efficient text extraction and pattern matching
- **Processing Time**: Typical analysis completes in 2-5 seconds
- **Concurrency**: Supports multiple simultaneous uploads

## Security Features

- **File Validation**: Strict PDF format checking
- **Size Limits**: Maximum 10MB file size
- **Content Scanning**: No malicious content detection
- **Data Privacy**: Transaction data not permanently stored
- **User Isolation**: Strict user-based access control

## Monitoring and Logging

### Success Metrics
- Transaction extraction success rate
- Bank format detection accuracy
- Tier assignment distribution
- Quality score averages

### Error Tracking
- PDF parsing failures
- Unsupported bank formats
- Quality score trends
- User feedback correlation

This enhanced system provides a much more user-friendly experience while maintaining the sophisticated financial analysis capabilities of the StockFellow platform.
