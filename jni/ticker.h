#ifndef TICKER_H
#define TICKER_H

#define RESOLUTION 480

void initTicker();

long loopBeginTick, loopEndTick;
long loopBeginSample, loopEndSample;

float BPM;
long MSPT; // microseconds per tick
long SPT;  // samples per tick

#endif // TICKER_H
