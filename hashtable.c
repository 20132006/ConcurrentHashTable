/** \CSE412 Assignment_1
 *  \author Jaeyoung Yun
 *  \sinsunby@unist.ac.kr
 *  \modified Kyu Yeun Kim
 *  \kyuyeunk@unist.ac.kr
 */

//simplicity, speed, strength
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <time.h>
#include <pthread.h>
#include "hashtable.h"



enum operation{
    //enumeration of hashtable operation types
    INSERT = 1,	LOOKUP = 2, DELETE = 3
}; 

struct element{
    // hashtable element
    struct element *next;
    int key;
};

struct bucket_t{ 
    //hashtable bucket
    struct element *chain;
    pthread_mutex_t mutex_lookup;
    pthread_mutex_t mutex_DeleteInsert;
};

struct hashtable{ //hashtable
    int num_bucket;
    struct bucket_t *bucket;
};

struct arg_struct{
    //passing argument struct
    H_table_t hash_table;
    int thr_id;
};

struct data_t{ 
    //specified data type
    int op; // hash table operation type
    int key; // key value
};

// a pointer variable pointing to the sequence of
// hash table operations performed by multiple threads
struct data_t *data;

int num_op; // total number of operations to be performed
int nthr; // number of threads
int numLookup[100];

int hashtable_init(H_table_t *hash_table, int numbuckets){

    //TODO: Initialize the hash table with specified number of buckets.
    //printf("hashtable_init running...\n");
    //printf("%d\n", numbuckets);
    //(*hash_table)->bucket = malloc(numbuckets * sizeof(bucket_Type));
    //printf("hashtable_init running... %d\n", numbuckets);
    (*hash_table)->num_bucket = numbuckets;
    int i;

    //printf("number of buckets %d", numbuckets);
    (*hash_table)->bucket = malloc(numbuckets * sizeof(struct bucket_t));
    //printf ("successfully malloc\n");
    for (i=0;i<numbuckets;i++)
    {
        numLookup[i] = 0;
        (*hash_table)->bucket[i].chain = NULL;
	pthread_mutex_init( &((*hash_table)->bucket[i].mutex_lookup), NULL);
	pthread_mutex_init( (&(*hash_table)->bucket[i].mutex_DeleteInsert), NULL);
    }
    //(*hash_table)->bucket = bucket_;
    return 0;
}

int hashtable_insert(H_table_t *hash_table, int key){

    //TODO: Insert a key value to the hash table. 
    //If the key value has been successfully added,
    //return 0; otherwise, return -1
    //duplicates are allowed.
    //printf("element INSERT called %d\n", key);
    int index = key % (*hash_table)->num_bucket;

    pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<----------------- lock
    

    //printf("Locked %d\n", index );
    struct element *elem = malloc (sizeof(struct element));
    elem->key = key;
    //elem->next = elem;
    //printf("variable A is at address: %p\n", (void*)(*hash_table)->bucket[index].chain);
    if (!(*hash_table)->bucket[index].chain)
    {
	elem->next = NULL;
        (*hash_table)->bucket[index].chain = elem;
	//printf("Locked Released form NULL%d\n", index );
        pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<----------- unlock
        return 0;
    }
    else
    {
        elem->next = (*hash_table)->bucket[index].chain->next;
        (*hash_table)->bucket[index].chain->next = elem;

        //printf("Locked Released INSERT NOT FULL%d\n", index );
        pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<----------- unlock
        return 0;
    }

    //printf("Locked Released FAIL TO INSERT%d\n", index );
    pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<--------------- unlock
    //printf("element INSERT fail %d\n", key);
    return -1;
}

int hashtable_lookup(H_table_t *hash_table, int key){

    //TODO: Lookup the key. If it is in the hash table, 
    //return 0; otherwise, return -1
    //printf("element Lookup called %d\n", key);

    struct element *ptr;
    struct element *node;
    int index = key % (*hash_table)->num_bucket;

    pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_lookup) );
    
    numLookup[index]++;
    if (numLookup[index] == 1)
        pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_DeleteInsert) );

    pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_lookup) );

    //printf("LOOKUP running...\n");
    if ( !((*hash_table)->bucket[index].chain) )
    {
        pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_lookup) );

        numLookup[index]--;
        if (numLookup[index] == 0)
            pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) );
        
        pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_lookup) );
        return -1;
    }
    
    ptr = (*hash_table)->bucket[index].chain;
    //printf("LOOKUP running...\n");

    while (ptr){
        if (ptr->key == key)
        {
            //printf("LOOKUP running...\n");
            //printf("element Lookup exist %d\n", key);
            pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_lookup) );
            
            numLookup[index]--;
            if (numLookup[index] == 0)
                pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) );
            
            pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_lookup) );
            return 0;
        }
	ptr =ptr->next;
        //printf("LOOKUP running...\n");
    }
    pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_lookup) );
    
    numLookup[index]--;
    if (numLookup[index] == 0)
        pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) );
    
    pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_lookup) );
    //printf("element Lookup fail %d\n", key);
    return -1;
}

int hashtable_delete(H_table_t *hash_table, int key){

    //TODO: Delete all elements with the key.
    //If it is in the hash table,
    //return 0; otherwise, return -1
    //printf("element delete called %d\n", key);
    struct element *ptr;
    struct element *node;
    int number_B;
    number_B = ((*hash_table)->num_bucket);
    int index = key % number_B;
    pthread_mutex_lock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<--------------------- lock
	
    //printf("Locked %d \n", index );
    //printf("DELETE running...\n");
    if (!((*hash_table)->bucket[index].chain))
    {
	//printf("Locked Released NULL POINER %d\n", index );
	pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<----------- unlock
        return -1;
    }
    //printf("variable A is at address: %p\n", (void*)(*hash_table)->bucket[index].chain);
    //printf("variable A is at address: %p\n", (void*)(*hash_table)->bucket[index].chain->next);
    ptr = (*hash_table)->bucket[index].chain;
    node = ptr->next;
    if (ptr->key == key)
    {
	if (!node)
	{
		(*hash_table)->bucket[index].chain = NULL;
		free(ptr);
		pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<------- unlock
		return 0;
	}
	else
	{
		(*hash_table)->bucket[index].chain = node;
		free(ptr);
		pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<------- unlock
                return 0;
	}
    }
    bool first_time = true;
    while (true){
	//printf("While Loop\n");
	if (!node)
		break;
        else if(node->key == key)
        {
            ptr->next = node->next;
            free(node);
	    node = NULL;
            //printf("Locked Released DELETED %d\n", index );
	    pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<----------- unlock
            return 0;
        }
        ptr = node;
        node = ptr->next;
        first_time = false;
    }
    //printf("Locked Released FAIL TO FIND %d\n", index );
    pthread_mutex_unlock( &((*hash_table)->bucket[index].mutex_DeleteInsert) ); //<------------------- unlock
    return -1;
}

void *test(void *arguments){
    // pthread Function
    int i, rc, thr_id;
    int start, end;
    H_table_t hash_table;
    //TODO: Assign each variable with a correct value
    //use arguments to get hash table structure and thr_id
    //printf("test called\n");
    struct arg_struct *argum;
    argum  = (struct arg_struct *) arguments;
    thr_id = argum->thr_id;
    hash_table = argum->hash_table;
    int eachTO = 0;
    if (num_op%nthr != 0)
	eachTO = (num_op/nthr)+1;
    else
	eachTO = num_op/nthr;
    start = thr_id*eachTO;
    end = (thr_id+1)*eachTO;
    if (end > num_op)
        end = num_op;
    //printf("test running start end thr_id:%d %d %d\n",start, end, thr_id);
    for(i= start; i< end; i++){
	//printf("test running...\n");
        if(data[i].op == INSERT)
            rc=hashtable_insert(&hash_table, data[i].key);
        else if(data[i].op == LOOKUP)
            rc=hashtable_lookup(&hash_table, data[i].key);
        else if(data[i].op == DELETE)
            rc=hashtable_delete(&hash_table, data[i].key);
	//printf("test running...\n");
    }
    return 0;
}


int main(int argc, char** argv){
    struct timespec time_info;
    const int hash_num_bucket = atoi(argv[1]);
    num_op = atoi(argv[2]);
    nthr = atoi(argv[3]);
    const int i_ratio = atoi(argv[4]);
    const int d_ratio = atoi(argv[5]);
    //printf ("hash_num bucket:%d\n num_op:%d\n nthr:%d\n i_ratio:%d d_ratio:%d\n", hash_num_bucket, num_op, nthr, i_ratio, d_ratio);
    int i, thr_id, status, result=0;
    int64_t start_time, end_time;
    H_table_t hash_table;

    pthread_t *p_thread = malloc(nthr * sizeof(pthread_t));
    struct arg_struct *args = malloc(nthr * sizeof(struct arg_struct));

    // 1. Data generation phase
    data = malloc(num_op * sizeof(struct data_t));	
    srand(22);
    for(i=0; i<num_op; i++){ //make a data set
        int r = rand()%65536;

        data[i].key = r;
        r = rand()%100;
        if(r < i_ratio)
            data[i].op = INSERT;
        else if(r < i_ratio + d_ratio)
            data[i].op = DELETE;
        else
            data[i].op = LOOKUP;
    }
    printf("data success\n");

    hash_table = (H_table_t)malloc(sizeof(struct hashtable));
    hashtable_init(&hash_table, hash_num_bucket); //Initialize the hash table
    printf("init success\n");

    // 2. Performance test
    if(	clock_gettime( CLOCK_REALTIME, &time_info ) == -1 ){ 
        //record start point
        perror( "clock gettime" );
        exit( EXIT_FAILURE );
    }
    start_time = ( (int64_t) time_info.tv_sec * 1000000000 + (int64_t) time_info.tv_nsec );
    //printf("Start creating threads\n");
    for(thr_id=0; thr_id<nthr; thr_id++){
        //make threads and pass the arguments
        //TODO: create thread by pthread_create()
        // use arg_struct to pass the multiple arguments
        // (thread_id, hash_table)
	//printf("Start creating threads\n");
        args[thr_id].hash_table = hash_table;
        args[thr_id].thr_id = thr_id;
	//printf("args Start creating threads\n");
        pthread_create(&p_thread[thr_id], NULL, test, (void*) &args[thr_id]);
    }
    //printf("Start creating threads\n");
    for(thr_id=0; thr_id<nthr; thr_id++){
        //TODO: use pthread_join to wait till the thread terminate
        pthread_join(p_thread[thr_id],NULL);
    }
    if(	clock_gettime( CLOCK_REALTIME, &time_info ) == -1 ){ //record end point
        perror( "clock gettime" );
        exit( EXIT_FAILURE );
    }
    end_time = ( (int64_t) time_info.tv_sec * 1000000000 + (int64_t) time_info.tv_nsec );

    printf("Running Time: %lf s\n", (double)((long)end_time-(long)start_time)/1000000000.0);

    // 3. Correctness test
    // DO NOT MODIFY THE CODE BELOW
    //printf("program running\n");
    int key;
    for(key=0; key<65536; key++){ //simple test for the Hash table
        bool no_op=true;
        enum operation keys_op;
        //printf("program running\n");
        for(i=0; i<num_op; i++){
            //printf("program running - %d\n", i);
            if(data[i].key==key){
                if(no_op==true){
                    keys_op=data[i].op;
                    no_op=false;
                }
                else if(keys_op!=data[i].op){
                    break;
                }
            }
        }
        if(i==num_op){
	    //printf("program running for hashtable_lookup\n");
            int lookup = hashtable_lookup(&hash_table, key);
            if((no_op && lookup==0) || (no_op==false && keys_op==INSERT && lookup==-1) || (no_op==false && keys_op==DELETE && lookup==0)){
                result=-1;
            }
        }
    }
    //result of test
    if(result==-1)
        printf("Correctness: Incorrect\n");
    else
        printf("Correctness: Correct\n");
    return 0;
}
