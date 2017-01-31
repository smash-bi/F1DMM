java -cp aeron-all-1.0.4.jar \
    -Dagrona.disable.bounds.checks=true \
    -Daeron.sample.frameCountLimit=256 \
    io.aeron.samples.RateSubscriber
