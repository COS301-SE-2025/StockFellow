import pdfplumber
import pandas as pd
import re
from datetime import datetime

def analyze_pdf_type(pdf_path):
    """Determine if PDF has extractable text or requires OCR"""
    try:
        with pdfplumber.open(pdf_path) as pdf:
            text = ""
            # Try first 2 pages to determine type
            for page in pdf.pages[:2]:
                page_text = page.extract_text()
                if page_text:
                    text += page_text
            
            # Check if we got meaningful text
            if len(text.strip()) > 100 and any(char.isalnum() for char in text):
                return "text_based"
            else:
                return "image_based"
                
    except Exception as e:
        print(f"Error analyzing PDF: {e}")
        return "error"

def is_account_table(table):
    """Determine if a table contains account data vs header info"""
    header_row = table[0] if table[0] else []
    account_info_headers = [
        'branch number', 'branch',
        'account number', 'account',
        'date', 
    ]

    header_matches = 0
    for header in header_row:
        if header and isinstance(header, str):
            header_lower = header.lower().strip()
            for info_header in account_info_headers:
                if info_header in header_lower:
                    header_matches += 1
                    break
    
    # If we found 2+ transaction-related headers, likely a transaction table
    is_info_table = header_matches >= 2
    print(f"       📋 Header analysis: {header_matches} account-related headers found")
    print(f"       📋 Headers: {header_row}")
    
    return is_info_table


def is_transaction_table(table):
    """Determine if a table contains transaction data vs header info"""
    if not table or len(table) < 3:  # Need at least header + 2 data rows
        return False
    
    header_row = table[0] if table[0] else []
    
    # Look for transaction-like headers
    transaction_headers = [
        'date', 'transaction', 'description', 'amount', 'balance', 
        'debit', 'credit', 'withdrawal', 'deposit', 'memo', 'reference',
        'trans date', 'posting date', 'effective date'
    ]
    
    header_matches = 0
    for header in header_row:
        if header and isinstance(header, str):
            header_lower = header.lower().strip()
            for trans_header in transaction_headers:
                if trans_header in header_lower:
                    header_matches += 1
                    break
    
    # If we found 2+ transaction-related headers, likely a transaction table
    is_trans_table = header_matches >= 2
    print(f"       📋 Header analysis: {header_matches} transaction-related headers found")
    print(f"       📋 Headers: {header_row}")
    
    return is_trans_table

def find_column_index(header_row, possible_names):
    """Find column index by matching header names"""
    print(f"🔍 Looking for columns matching {possible_names}")
    
    if not header_row:
        print("❌ No header row provided")
        return None
    
    for i, header in enumerate(header_row):
        if header and isinstance(header, str):
            header_lower = header.lower().strip()
            print(f"   Column {i}: '{header}' -> '{header_lower}'")
            for name in possible_names:
                if name in header_lower:
                    print(f"     ✅ Match found for '{name}'")
                    return i
        else:
            print(f"   Column {i}: {type(header)} - {repr(header)}")
    
    print(f"❌ No matching column found for {possible_names}")
    return None

def process_table_data(table):
    """Process extracted table data into transactions"""
    print(f"📊 Processing table with {len(table)} rows")
    
    if not table or len(table) < 2:
        print("❌ Table too small or empty")
        return []
    
    transactions = []
    header_row = table[0] if table[0] else []
    print(f"🏷️ Header row: {header_row}")
    
    # Find column indices (common bank statement headers)
    date_col = find_column_index(header_row, ['date', 'transaction date', 'posted', 'trans date'])
    desc_col = find_column_index(header_row, ['description', 'transaction', 'details', 'memo'])
    amount_col = find_column_index(header_row, ['amount', 'debit', 'credit', 'withdrawal', 'deposit'])
    
    print(f"🔍 Column mapping: Date={date_col}, Description={desc_col}, Amount={amount_col}")
    
    if date_col is None or desc_col is None or amount_col is None:
        print("❌ Could not find required columns")
        return []
    
    # Process data rows
    amount_lines = []
    for row_num, row in enumerate(table[1:], 1):
        if not row or len(row) <= max(date_col, desc_col, amount_col):
            print(f"   Row {row_num}: Skipped (too short)")
            continue
            
        try:
            # Date 
            date_str = str(row[date_col]).strip() if row[date_col] else ""
            # Description
            if row_num != 1:
                description = str(row[desc_col]).strip() if row[desc_col] else "Unknown/Fee"
            else:
                description = str(row[desc_col]).strip() if row[desc_col] else ""
            # Amount
            if not amount_lines:
                amount_str = str(row[amount_col]).strip() if row[amount_col] else ""
            else:
                if row_num-2 < len(amount_lines):  # Check if index is valid
                    raw_amount = amount_lines[row_num-2]  # 1 for 0 index and 1 for 1st row being amounts
                    
                    # Check if amount ends with "Cr" or "C"
                    if raw_amount.upper().endswith('CR') or raw_amount.upper().endswith('C'):
                        amount_str = raw_amount.upper().replace('CR', '').replace('C', '').strip()
                    else:
                        clean_amount = raw_amount.strip()
                        amount_str = f"-{clean_amount}"
                else:
                    amount_str = ""

            # print(f"   Row {row_num}: Date='{date_str}', Desc='{description[:20]}', Amount='{amount_str[:50]}...'")

            # Private Print
            # print(f"   Row {row_num}: Date='{date_str}', Desc=***, Amount='{amount_str[:50]}...'")
            
            # Check if amount_str contains multiple amounts (newline separated)
            if '\n' in amount_str and not date_str and not description:
                print(f"     🔍 Found multi-line amounts, splitting...")
                amount_lines = [line.strip() for line in amount_str.split('\n') if line.strip()]
                print(f"     📊 Split into {len(amount_lines)} amounts: {amount_lines[:5]}...")
                
                continue
            
            # Normal single-row processing
            if not date_str or not description or not amount_str:
                print(f"     ⚠️ Skipped - missing data")
                continue
            
            # Parse date
            date = parse_date(date_str)
            if not date:
                print(f"     ❌ Could not parse date: {date_str}")
                continue
            
            # Parse amount
            amount = parse_amount(amount_str)
            # print(f"     💰 Parsed amount: {amount}")
            
            transactions.append({
                'date': date,
                'description': description,
                'amount': amount,
                'raw_data': row
            })
            # print(f"     ✅ Transaction added")
            
        except Exception as e:
            print(f"     ❌ Error processing row {row}: {e}")
            continue

    print(f"📈 Total transactions from table: {len(transactions)}")
    return transactions

def extract_text_pdf(pdf_path):
    """Extract structured data from text-based PDF using pdfplumber"""
    try:
        with pdfplumber.open(pdf_path) as pdf:
            print(f"📄 PDF has {len(pdf.pages)} pages")
            all_text = ""
            all_transactions = []
            
            for page_num, page in enumerate(pdf.pages):
                print(f"\n--- Processing Page {page_num + 1} ---")
                
                # Check if page has images (indicates scanned document)
                images = page.images
                print(f"    🖼️ Page has {len(images)} images")
                
                # Try table extraction first
                tables = page.extract_tables()
                print(f"    🔍 Found {len(tables)} tables on page {page_num + 1}")
                
                if tables:
                    for table_num, table in enumerate(tables):
                        print(f"    📊 Table {table_num + 1}: {len(table)} rows x {len(table[0]) if table else 0} cols")
                        
                        print("       First 5 rows of table:")
                        for i, row in enumerate(table[:5]):
                            print(f"         Row {i}: {row}")
                        
                        # Check table type
                        if is_transaction_table(table):
                            print("          ✅ This looks like a transaction table!")
                            transactions = process_table_data(table)
                            if transactions:
                                all_transactions.extend(transactions)
                        # elif is_account_table(table): 
                        #     print("   ⚠️ This looks like an account info table, skipping")
                        else:
                            print("          ⚠️ This looks like a header/info table, skipping")
                
                # Always extract text as fallback
                page_text = page.extract_text()
                if page_text:
                    print(f"📝 Standard extraction: {len(page_text)} characters")
                    
                    # Show more sample text to see the pattern
                    print(f"📄 Full text sample (first 2000 chars):")
                    print(page_text[:2000])
                    print("=" * 50)
                    
                    all_text += page_text + "\n"
                
                # Try extracting words with coordinates for better structure detection
                words = page.extract_words()
                print(f"🔤 Found {len(words)} individual words")
                if words and len(words) > 20:  # Only show if we have substantial words
                    print("   Sample words with positions:")
                    for word in words[10:25]:  # Skip header words, show middle content
                        print(f"     '{word['text']}' at ({word['x0']:.1f}, {word['top']:.1f})")
            
            # If we found transactions in tables, return those
            if all_transactions:
                print(f"🎯 Returning {len(all_transactions)} transactions from tables")
                return pd.DataFrame(all_transactions)
            
            print(f"\n🔤 No table transactions found, trying text parsing...")
            print(f"🔤 Total text length: {len(all_text)} characters")
            
            # Parse the extracted text
            df = parse_bank_statement_text(all_text)
            print(f"🎯 Final result: {len(df)} transactions found via text parsing")
            return df
            
    except Exception as e:
        print(f"❌ Error extracting from PDF: {e}")
        return pd.DataFrame()

def parse_date(date_str):
    """Parse various date formats"""
    if not date_str:
        return None
        
    date_formats = [
        '%m/%d/%Y', '%m/%d/%y',
        '%d/%m/%Y', '%d/%m/%y', 
        '%Y-%m-%d', '%Y/%m/%d',
        '%d-%m-%Y',
        '%d %b %Y', '%d %B %Y',
        '%d %b',    
        '%d %B',     
    ]
    
    for fmt in date_formats:
        try:
            parsed_date = datetime.strptime(date_str.strip(), fmt)
            
            if parsed_date.year == 1900:
                # You can either use current year or extract year from statement
                current_year = 2025  # or datetime.now().year
                parsed_date = parsed_date.replace(year=current_year)
                # print(f"     📅 Added year {current_year} to date: {parsed_date}")
            
            return parsed_date
        except ValueError:
            continue
    
    return None

def parse_amount(amount_str):
    """Convert amount string to float"""
    if not amount_str:
        return 0.0
    
    # Remove currency symbols, commas, spaces
    clean_amount = re.sub(r'[^\d.-]', '', str(amount_str))
    
    # Handle negative amounts in parentheses like (100.00)
    if '(' in str(amount_str) and ')' in str(amount_str):
        clean_amount = '-' + clean_amount
    
    try:
        return float(clean_amount)
    except (ValueError, TypeError):
        return 0.0

def parse_bank_statement_text(text):
    """Parse bank statement text into structured data"""
    print(f"\n🔍 Starting text parsing...")
    transactions = []
    
    # Transaction line patterns
    transaction_patterns = [
        r'(\d{1,2}/\d{1,2}/\d{4})\s+(.+?)\s+([-+]?\$?[\d,]+\.?\d*)\s*$',
        r'(\d{4}-\d{2}-\d{2})\s+(.+?)\s+([-+]?\$?[\d,]+\.?\d*)\s*$',
        r'(\d{1,2}/\d{1,2}/\d{4})\s+(.+?)\s+([-+]?\$?[\d,]+\.?\d*)\s+([-+]?\$?[\d,]+\.?\d*)',  # Date, Desc, Amount, Balance
    ]
    
    lines = text.split('\n')
    print(f"📝 Processing {len(lines)} lines of text")
    
    pattern_matches = {i: 0 for i in range(len(transaction_patterns))}
    
    for line_num, line in enumerate(lines):
        line = line.strip()
        if len(line) < 10:  # Skip short lines
            continue
            
        # Show some sample lines for debugging
        if line_num < 30:
            print(f"   Line {line_num}: {repr(line)}")
            
        for pattern_idx, pattern in enumerate(transaction_patterns):
            match = re.search(pattern, line)
            if match:
                pattern_matches[pattern_idx] += 1
                try:
                    date_str = match.group(1)
                    description = match.group(2).strip()
                    amount_str = match.group(3)
                    
                    print(f"✅ Match found on line {line_num}: Date='{date_str}', Desc='{description[:30]}', Amount='{amount_str}'")
                    
                    # Parse date
                    date = parse_date(date_str)
                    if not date:
                        print(f"   ❌ Could not parse date: {date_str}")
                        continue
                    
                    # Parse amount
                    amount = parse_amount(amount_str)
                    print(f"   💰 Parsed amount: {amount}")
                    
                    # Skip if we couldn't parse essential data
                    if not description or amount == 0:
                        print(f"   ⚠️ Skipping - missing data or zero amount")
                        continue
                    
                    transactions.append({
                        'date': date,
                        'description': description,
                        'amount': amount,
                        'raw_line': line
                    })
                    break
                    
                except Exception as e:
                    print(f"   ❌ Error parsing line '{line}': {e}")
                    continue
    
    print(f"\n📊 Pattern match summary:")
    for i, count in pattern_matches.items():
        print(f"   Pattern {i}: {count} matches")
    
    print(f"🎯 Total transactions found: {len(transactions)}")
    
    return pd.DataFrame(transactions)

def validate_and_clean_transactions(df):
    """Clean and validate extracted transaction data"""
    if df.empty:
        return df
    
    # Remove rows with missing essential data
    df = df.dropna(subset=['date', 'description'])
    
    # Remove duplicates
    #df = df.drop_duplicates(subset=['date', 'description', 'amount'])
    
    # Sort by date
    df = df.sort_values('date')
    
    # Basic categorization
    df['category'] = df['description'].apply(categorize_transaction)
    
    return df

def categorize_transaction(description):
    """Basic transaction categorization"""
    if not description:
        return 'other'
    
    description_lower = description.lower()
    
    categories = {
        'income': ['salary', 'payroll', 'deposit', 'transfer in', 'interest'],
        'housing': ['rent', 'mortgage', 'housing'],
        'food': ['grocery', 'restaurant', 'food', 'dining'],
        'utilities': ['electric', 'gas', 'water', 'internet', 'phone'],
        'transport': ['fuel', 'gas station', 'uber', 'taxi', 'parking'],
        'entertainment': ['netflix', 'streaming', 'movie', 'entertainment'],
        'shopping': ['amazon', 'store', 'purchase', 'retail'],
        'banking': ['fee', 'charge', 'atm', 'overdraft']
    }
    
    for category, keywords in categories.items():
        if any(keyword in description_lower for keyword in keywords):
            return category
    
    return 'other'

def calculate_summary_metrics(df):
    """Calculate affordability metrics from transactions"""
    if df.empty:
        return {}
    
    income_transactions = df[df['amount'] > 0]
    expense_transactions = df[df['amount'] < 0]
    
    total_income = income_transactions['amount'].sum()
    total_expenses = abs(expense_transactions['amount'].sum())
    
    # Calculate monthly averages (assuming 3-month period)
    months = 3
    
    return {
        'total_income': total_income,
        'total_expenses': total_expenses,
        'net_income': total_income - total_expenses,
        'avg_monthly_income': total_income / months,
        'avg_monthly_expenses': total_expenses / months,
        'avg_monthly_net': (total_income - total_expenses) / months,
        'transaction_count': len(df),
        'income_count': len(income_transactions),
        'expense_count': len(expense_transactions),
        'categories': df['category'].value_counts().to_dict(),
        'date_range': {
            'start': df['date'].min(),
            'end': df['date'].max()
        }
    }

def extract_bank_statement_data(pdf_path):
    """Main function to extract bank statement data"""
    print(f"Analyzing PDF: {pdf_path}")
    
    # Step 1: Determine PDF type
    pdf_type = analyze_pdf_type(pdf_path)
    print(f"PDF type: {pdf_type}")
    
    if pdf_type == "error":
        return {'error': 'Could not analyze PDF'}
    
    # Step 2: Extract data
    if pdf_type == "text_based":
        df = extract_text_pdf(pdf_path)
        print(f"Extracted {len(df)} transactions")
    else:
        print("Image-based PDF detected. OCR implementation needed.")
        return {'error': 'OCR not implemented yet'}
    
    if df.empty:
        return {'error': 'No transactions found'}
    
    # Step 3: Clean and validate
    df = validate_and_clean_transactions(df)
    
    # Step 4: Calculate summary
    summary = calculate_summary_metrics(df)
    
    return {
        'transactions': df,
        'summary': summary,
        'extraction_method': pdf_type,
        'transaction_count': len(df)
    }

# Test the extraction
if __name__ == "__main__":
    result = extract_bank_statement_data('FNBY NEXT TRANSACT ACCOUNT 70.pdf')
    
    if 'error' in result:
        print(f"❌ Error: {result['error']}")
    else:
        print("✅ Extraction successful!")
        print(f"Found {result['transaction_count']} transactions")
        print("\nSummary:")
        summary = result['summary']
        print(f"Total income: R{summary['total_income']:.2f}")
        print(f"Total expenses: R{summary['total_expenses']:.2f}")
        print(f"Average monthly income: R{summary['avg_monthly_income']:.2f}")
        print(f"Average monthly expenses: R{summary['avg_monthly_expenses']:.2f}")
        print(f"Average monthly net: R{summary['avg_monthly_net']:.2f}")
        
        print(f"\nTransaction categories:")
        for category, count in summary['categories'].items():
            print(f"  {category}: {count} transactions")
        
        # Show first few transactions
        print(f"\nFirst 5 transactions:")
        for i, row in result['transactions'].head().iterrows():
            print(f"  {row['date'].strftime('%Y-%m-%d')} | {row['description'][:30]:<30} | R{row['amount']:>8.2f}")