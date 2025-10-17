# Payer Details #
### Fields: ###
- ```Signature```: Uniquely identifies the users card as a hash of the card details
- ```Authorization```: A unique token that represents the saved card/payment method and is used to charge the customers card on a recurring basis after being authenticated with a small transaction


# Group Cycle#
### Phases ###
- pending - not implemented?
- active - initiatal creation while transactions are being created??
- collecting - accepting transactions
- collection_complete - wont accept collection
- payout_pending - attempting payout
- completed - payout successful
- failed - payout failed or other error
- cancelled - cancelled

# Transaction # 
### Status ###
- pending
- comleted
- failed
- cancelled
- processing
- refunded