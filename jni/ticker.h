#ifndef TICKER_H
#define TICKER_H

#include <stdint.h>

void initTicker();

// Java sets the loop tick
long loopBeginTick;
long loopEndTick;

// nanoseconds per tick
long NSPT;
// startTicking() keeps track of this currTick
long currTick;

#endif // TICKER_H
