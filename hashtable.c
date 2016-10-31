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

enum operation{ //enumeration of hashtable operation types
    INSERT = 1,	LOOKUP = 2, DELETE = 3
}; 

struct element{ // hashtable element
    struct element *next;
    int key;
};

struct bucket_t{ //hashtable bucket
    struct element *chain;
    pthread_mutex_t mutex; 
};

struct hashtable{ //hashtable
    int num_bucket;
    struct bucket_t *bucket;
};

struct arg_struct{ //passing argument struct
    H_table_t hash_table;
    int thr_id;
};

struct data_t{ //specified data type
    int op; // hash table operation type
    int key; // key value
};

// a pointer variable pointing to the sequence of
// hash table operations performed by multiple threads
struct data_t *data;

int num_op; // total number of operations to be performed
int nthr; // number of threads

int hashtable_init(H_table_t hash_table, int numbuckets){

    //TODO: Initialize the hash table with specified number of buckets.



}

int hashtable_insert(H_table_t hash_table, int key){

    //TODO: Insert a key value to the hash table. 
    //If the key value has been successfully added,
    //return 0; otherwise, return -1
    //duplicates are allowed.


}

int hashtable_lookup(H_table_t hash_table, int key){

    //TODO: Lookup the key. If it is in the hash table, 
    //return 0; otherwise, return -1


}

int hashtable_delete(H_table_t hash_table, int key){

    //TODO: Delete all elements with the key.
    //If it is in the hash table,
    //return 0; otherwise, return -1

}

void *test(void *arguments){ // pthread Function
    int i, rc, thr_id;
    int start, end;
    H_table_t hash_table;
    //TODO: Assign each variable with a correct value
    //use arguments to get hash table structure and thr_id

    for(i= start; i< end; i++){
        if(data[i].op == INSERT)
            rc=hashtable_insert(hash_table, data[i].key);
        else if(data[i].op == LOOKUP)
            rc=hashtable_lookup(hash_table, data[i].key);
        else if(data[i].op == DELETE)
            rc=hashtable_delete(hash_table, data[i].key);
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
    hashtable_init(hash_table, hash_num_bucket); //Initialize the hash table
    printf("init success\n");

    // 2. Performance test
    if(	clock_gettime( CLOCK_REALTIME, &time_info ) == -1 ){ //record start point
        perror( "clock gettime" );
        exit( EXIT_FAILURE );
    }
    start_time = ( (int64_t) time_info.tv_sec * 1000000000 + (int64_t) time_info.tv_nsec );

    for(i=0; i<nthr; i++){ //make threads and pass the arguments
        //TODO: create thread by pthread_create()
        // use arg_struct to pass the multiple arguments
        // (thread_id, hash_table)
    }
    for(i=0; i<nthr; i++){
        //TODO: use pthread_join to wait till the thread terminate
    }

    if(	clock_gettime( CLOCK_REALTIME, &time_info ) == -1 ){ //record end point
        perror( "clock gettime" );
        exit( EXIT_FAILURE );
    }
    end_time = ( (int64_t) time_info.tv_sec * 1000000000 + (int64_t) time_info.tv_nsec );

    printf("Running Time: %lf s\n", (double)((long)end_time-(long)start_time)/1000000000.0);

    // 3. Correctness test
    // DO NOT MODIFY THE CODE BELOW

    int key;
    for(key=0; key<65536; key++){ //simple test for the Hash table
        bool no_op=true;
        enum operation keys_op;
        for(i=0; i<num_op; i++){
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
            int lookup = hashtable_lookup(hash_table, key);
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
