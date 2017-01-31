java -cp aeron-all-1.0.4.jar \
    -XX:BiasedLockingStartupDelay=0 \
    -Daeron.mtu.length=16384 \
    -Daeron.socket.so_sndbuf=2097152 \
    -Daeron.socket.so_rcvbuf=2097152 \
    -Daeron.rcv.initial.window.length=2097152 \
    -Dagrona.disable.bounds.checks=true \
    io.aeron.samples.LowLatencyMediaDriver
