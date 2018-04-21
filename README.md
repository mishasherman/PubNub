# PubNub
AGT exam
Exam was written with Java using ItelliJ IDEA.

Classes are stored under src/main/java :
  - PubNubPubSub which wraps PubNub API calls
  - SubscribeCallbackWithReconnect which is used as PubNub listener
  
Test file is src/test/java/PubNubTest
It contains 2 test scenarios :
  - testChannelSubscribeUnsubscribe
  - testChannelHistory
  
I used to run each scenario from within ItelliJ IDEA 
  - right-click on scenario name (testChannelSubscribeUnsubscribe) -> Run
