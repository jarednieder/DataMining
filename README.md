DataMining
==========

**Running a MapReduce Task**  
1. Create Java file containing public static classes implementing Mapper and a Reducer and main method w/ configuration.  
2. (In Eclipse) Export the project to a JAR (File > Export)  
3. Run the JAR using the hadoop command `bin/hadoop jar *path-to-jar*`  
**(Note: Hadoop must be running (`bin/start-all.sh`) prior to calling step 3)**  
