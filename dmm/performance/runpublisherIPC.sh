java -cp aeron-all-1.0.4.jar \
    -Dagrona.disable.bounds.checks=true \
    -Daeron.sample.messageLength=1024 \
    io.aeron.samples.EmbeddedIpcThroughput
