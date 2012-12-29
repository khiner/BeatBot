#ifndef EFFECTS_H
#define EFFECTS_H

typedef struct Effect_t {
	void *config;
	void (*set)(void *, float, float);
	void (*process)(void *, float **, int);
	void (*destroy)(void *);
	bool on;
} Effect;

typedef struct EffectNode_t {
	Effect *effect;
	struct EffectNode_t *next;
} EffectNode;

Effect *initEffect(bool on, void *config,
		void (*set), void (*process), void (*destroy));

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);

#endif // EFFECTS_H
