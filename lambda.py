import json

def lambda_handler(event, context):
    # Check if the request is a GET request 
    if event.get('requestContext', {}).get('http', {}).get('method') == 'GET':
        return {
            'statusCode': 200,
            'headers': {
                'Content-Type': 'text/html' 
            },
            'body': '<h1>Hello from Lambda!</h1><p>Your GET request was successful.</p>'
        }
    
    return {
        'statusCode': 400,
        'body': json.dumps('Unsupported request method')
    }