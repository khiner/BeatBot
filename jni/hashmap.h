#ifndef HASHMAP_H
#define HASHMAP_H

#include <assert.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <sys/types.h>

typedef struct Entry Entry;
struct Entry {
    void* key;
    int hash;
    void* value;
    Entry* next;
};

typedef struct Hashmap {
    Entry** buckets;
    size_t bucketCount;
    int (*hash)(void* key);
    bool (*equals)(void* keyA, void* keyB);
    size_t size;
} Hashmap;

Hashmap* hashmapCreate(size_t initialCapacity);

static inline int hashKey(Hashmap* map, void* key);
size_t hashmapSize(Hashmap* map);
static inline size_t calculateIndex(size_t bucketCount, int hash);
static void expandIfNecessary(Hashmap* map);
void hashmapFree(Hashmap* map);
int hashmapHash(void* key, size_t keySize);
static inline bool equalKeys(void* keyA, int hashA, void* keyB, int hashB,
							 bool (*equals)(void*, void*));
void* hashmapPut(Hashmap* map, void* key, void* value);
void* hashmapGet(Hashmap* map, void* key);
bool hashmapContainsKey(Hashmap* map, void* key);
void* hashmapMemoize(Hashmap* map, void* key, 
					 void* (*initialValue)(void* key, void* context), void* context);
void* hashmapRemove(Hashmap* map, void* key);
void hashmapForEach(Hashmap* map, 
					bool (*callback)(void* key, void* value, void* context),
					void* context);
size_t hashmapCurrentCapacity(Hashmap* map);
size_t hashmapCountCollisions(Hashmap* map);
int hashmapIntHash(void* key);
bool hashmapIntEquals(void* keyA, void* keyB);

#endif // HASHMAP_H