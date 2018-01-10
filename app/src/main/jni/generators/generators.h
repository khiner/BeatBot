#ifndef GENERATORS_H
#define GENERATORS_H

typedef struct Generator_t {
    void *config;

    void (*set)(void *);

    void (*generate)(void *, float **, int);

    void (*destroy)(void *);
} Generator;

static inline void initGenerator(Generator *generator, void *config,
                                 void (*set), void (*generate), void (*destroy)) {
    generator->config = config;
    generator->set = set;
    generator->generate = generate;
    generator->destroy = destroy;
}

#endif // GENERATORS_H
