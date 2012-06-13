#ifndef TICKER_H
#define TICKER_H

#include <stdint.h>

void initTicker();

// nanoseconds per tick
static long NSPT = 100;

// Java sets the loop tick
long loopBeginTick;
long loopEndTick;

// startTicking() keeps track of this currTick
long currTick;

#endif // TICKER_H
