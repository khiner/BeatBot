#ifndef TICKER_H
#define TICKER_H

#include <stdint.h>

#define RESOLUTION 480

void initTicker();

// Java sets the loop tick
long loopBeginTick;
long loopEndTick;

float BPM;
// nanoseconds per tick
long NSPT;
// startTicking() keeps track of this currTick
long currTick;

#endif // TICKER_H
