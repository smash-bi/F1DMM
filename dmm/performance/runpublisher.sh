java -cp aeron-all-1.0.4.jar \
    -Daeron.sample.messageLength=2048 \
    -Daeron.sample.messages=500000000 \
    -Dagrona.disable.bounds.checks=true \
    io.aeron.samples.StreamingPublisher
