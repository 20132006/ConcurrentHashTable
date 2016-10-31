/** \CSE412 Assignment_1
 *  \author Jaeyoung Yun
 *  \sinsunby@unist.ac.kr
 *  \modified Kyu Yeun Kim
 *  \kyuyeunk@unist.ac.kr
 */

typedef struct hashtable *H_table_t;

int hashtable_init(H_table_t, int);

int hashtable_insert(H_table_t, int);

int hashtable_lookup(H_table_t, int);

int hashtable_delete(H_table_t, int);
