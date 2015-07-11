#ifndef TICKER_H
#define TICKER_H

void initTicker();

long loopBeginTick, loopEndTick, currTick, currSample;

float BPM;
long MSPT; // microseconds per tick
long SPT;  // samples per tick

#endif // TICKER_H
