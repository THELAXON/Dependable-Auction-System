cd server
rmiregistry &
sleep 1
java Replica 1 &
java Replica 2 &
java Replica 3 &
sleep 1 
java FrontEnd
