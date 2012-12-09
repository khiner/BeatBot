#ifndef TICKER_H
#define TICKER_H

void initTicker();

long loopBeginTick, loopEndTick;
long currSample, loopBeginSample, loopEndSample;

float BPM;
long MSPT; // microseconds per tick
long SPT;  // samples per tick

#endif // TICKER_H
