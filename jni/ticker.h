#ifndef TICKER_H
#define TICKER_H

#include <stdint.h>

#define RESOLUTION 480

void initTicker();

long loopBeginTick; // Java sets the loop tick
long loopEndTick;

float BPM;
long NSPT; // nanoseconds per tick
long currTick; // startTicking() keeps track of this currTick

#endif // TICKER_H
