package com.common.dbutils;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Title MongoDBUtil
 * @author GQ_Yin
 * */
public class MongoDao{
 
	private static final Logger logger = LoggerFactory.getLogger(MongoDao.class);
	public static MongoDatabase DB = null;
	
	
	public MongoDao(String dbUrl, String dataBase) {
		init(dbUrl, dataBase);
	}
	
	public MongoDao(String dbUrl, String dataBase, String username, String password) {
		init(dbUrl, dataBase, username, password);
	}
	
	
	
	/**
	 * 无认证连接数据库
	 * 
	 * @param dbUrl (localhost:27017,host2:27017,host3:27017)
	 * @param dataBase 数据库名称
	 */
    private void init(String dbUrl, String dataBase){
    	 MongoClientOptions.Builder build = new MongoClientOptions.Builder();
         build.connectionsPerHost(100); //最大连接数
         build.threadsAllowedToBlockForConnectionMultiplier(50);  //排队线程数
         build.maxWaitTime(1000 * 60 * 2);  //最大等待时间
         build.connectTimeout(1000 * 60 * 10); //最长连接时间
         MongoClientOptions mongoClientOptions = build.build();
         
         String[] urlArr = dbUrl.split(",");
         List<ServerAddress> addressList = new ArrayList<ServerAddress>();
         for (String url : urlArr) {
        	 ServerAddress serverAddress = new ServerAddress(url.split(":")[0], Integer.parseInt(url.split(":")[1]));
        	 addressList.add(serverAddress);
		 }
         
        //连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient(addressList, mongoClientOptions);
        
//       ServerAddress serverAddress = new ServerAddress("localhost", 27017);
//       MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://host1:27017,host2:27017,host3:27017"));
        //连接到数据库
        DB = mongoClient.getDatabase(dataBase);
    }
    
    
 
    /**
	 * 有认证连接数据库
	 * 
	 * @param dbUrl (localhost:27017,host2:27017,host3:27017)
	 * @param dataBase 数据库名称
	 * @param username 用户名
	 * @param password 密码
	 */
    private void init(String dbUrl, String dataBase, String username, String password){
    	MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        build.connectionsPerHost(100); //最大连接数
        build.threadsAllowedToBlockForConnectionMultiplier(50);  //排队线程数
        build.maxWaitTime(1000 * 60 * 2);  //最大等待时间
        build.connectTimeout(1000 * 60 * 10); //最长连接时间
        MongoClientOptions mongoClientOptions = build.build();
    	
        String[] urlArr = dbUrl.split(",");
        List<ServerAddress> addressList = new ArrayList<ServerAddress>();
        for (String url : urlArr) {
       	 ServerAddress serverAddress = new ServerAddress(url.split(":")[0], Integer.parseInt(url.split(":")[1]));
       	 addressList.add(serverAddress);
		 }
        
        List<MongoCredential> credentials = new ArrayList<>();
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, dataBase, password.toCharArray());
        credentials.add(mongoCredential);
        
        //通过连接认证获取MongoDB连接
        MongoClient mongoClient = new MongoClient(addressList, credentials, mongoClientOptions);
        //连接到数据库
        DB = mongoClient.getDatabase(dataBase);
    }
    
    
    /**
        * 直接返回DB连接对象
     * 
        * 适用现有方法无法满足查询时, 可自定义Query, insert, update, delete
     * */
    public MongoDatabase getDB() {
    	return DB;
    }
 
    
    
    /**
	 * 根据id检索文档
	 * 
	 * @param table
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> queryByID(String table, Object id) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		BasicDBObject query = new BasicDBObject("_id", id);
		// DBObject接口和BasicDBObject对象：表示一个具体的记录，BasicDBObject实现了DBObject，是key-value的数据结构，用起来和HashMap是基本一致的。
		FindIterable<Document> iterable = collection.find(query);
 
		Map<String, Object> map = new HashMap<String, Object>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			map.putAll(cursor.next());
		}
		logger.debug("检索ID完毕，db：{}，table：{}，id：{} ", DB.getName(), table, id);
 
		return map;
	}
 
 
	/**
	 * 根据doc检索文档集合，当doc是空的时候检索全部
	 * 
	 * @param table
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryByDoc(String table, Document doc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		FindIterable<Document> iterable = collection.find(doc);
		/**
		 * 1. 获取迭代器FindIterable<Document> 2. 获取游标MongoCursor<Document>   3.通过游标遍历检索出的文档集合
		 * */
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		logger.debug("检索doc完毕，db：{}，table：{}，doc：{} ", DB.getName(), table, doc.toJson());
		
		return list;
	}
	
	
	/**
	 * 根据条件检索文档集合
	 * 
	 * @param table
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryByFilters(String table, Bson filter) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		FindIterable<Document> iterable = collection.find(filter);
 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		return list;
	}
	
	/**
	 * 根据条件分页检索文档集合
	 * 
	 * @param table
	 * @param filter
	 * @param page 页数
	 * @param pageSize 每页数量
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryByFilters(String table, Bson filter, int page, int pageSize) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		FindIterable<Document> iterable = collection.find().skip(Math.abs(page)*Math.abs(pageSize)).limit(Math.abs(pageSize));
 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		return list;
	}
	
	
	/**
	 * 检索全部返回集合
	 * 
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryAll(String table) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		FindIterable<Document> iterable = collection.find();
 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		logger.info("检索全部完毕，db：{}，table：{}", DB.getName(), table);
		
		return list;
	}
	
	
	/**
	 * 检索全部返回集合（分页）
	 * 
	 * @param table
	 * @param page 页数
	 * @param pageSize 每页数量
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryAll(String table, int page, int pageSize) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		FindIterable<Document> iterable = collection.find().skip(page*pageSize).limit(pageSize);;
 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		logger.info("检索全部完毕，db：{}，table：{}", DB.getName(), table);
		
		return list;
	}
	
	
	/**
	 * 聚合查询(推荐)
	 * 
	 * @param table
	 * @param listDocument
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryByAggregate(String table, List<Document> listDocument) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		AggregateIterable<Document> iterable = collection.aggregate(listDocument);
 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(cursor.next());
			list.add(map);
		}
		logger.debug("检索doc完毕，db：{}，table：{}，doc：{} ", DB.getName(), table, listDocument);
		
		return list;
	}
 
	
	/**
	 * 遍历迭代器返回文档集合
	 * 
	 * @param iterable
	 * @return
	 * @throws Exception
	 */
	public List<Document> findIterable(FindIterable<Document> iterable) throws Exception {
		List<Document> list = new ArrayList<Document>();
		MongoCursor<Document> cursor = iterable.iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(doc);
		}
		cursor.close();
		return list;
	}
 
	
	/**
	 * 插入文档
	 * 
	 * @param table
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public boolean insert(String table, Document doc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		collection.insertOne(doc);
		long count = collection.count(doc);
		if (count >= 1) {
			logger.debug("文档插入成功，影响条数：{}，db：{}，table：{}，doc：{} ", count, DB.getName(), table, doc.toJson());
			return true;
		} else {
			logger.debug("文档插入失败，影响条数：{}，db：{}，table：{}，doc：{} ", count, DB.getName(), table, doc.toJson());
			return false;
		}
 
	}
 
	
	/**
	 * 插入多条文档(推荐)
	 * 
	 * @param table
	 * @param doc
	 * @param batch
	 * @return
	 * @throws Exception
	 */
	public boolean insertMany(String table, List<Document> doc, int batch) throws Exception {
 
		MongoCollection<Document> collection = DB.getCollection(table);
		long preCount = collection.count();
		
		int insertCollectionSize = doc.size();
		List<Document> insertList = new ArrayList<Document>();
		for(int i=0; i<insertCollectionSize; i++) {
			insertList.add(doc.get(i));
			if(insertList.size()>0 && i%batch==0 && i!=0) {
				collection.insertMany(insertList);
				insertList.clear();
				Thread.sleep(50);
			}
		}
		if(insertList.size()>0) {
			collection.insertMany(insertList);
			insertList.clear();
		}
 
		long nowCount = collection.count();
		if ((nowCount - preCount) == doc.size()) {
			logger.debug("文档插入成功，影响条数：{}，db：{}，table：{}", doc.size(), DB.getName(), table);
			return true;
		} else {
			logger.debug("文档插入失败，影响条数：{}，db：{}，table：{}", (nowCount - preCount), DB.getName(), table);
			return false;
		}
	}
	
	
	/**
	  * 批量保存或更新数据(存在时更新, 不存在时插入)
	  * 推荐使用
	 * 
	 * @param table
	 * @param primaryKey 集合主键
	 * @param docList
	 * @return
	 * @throws Exception
	 */
	public String saveOrUpdate(String table, String primaryKey, List<Document> docList) throws Exception {
		String ret = "no params";
		if(docList.size()>0) {
			MongoCollection<Document> collection = DB.getCollection(table);
			long preCount = collection.count();
			long modifiedCount = 0;
			long startTime = new Date().getTime();
			for (Document document : docList) {
				UpdateResult updateManyResult = collection.updateMany(Filters.eq(primaryKey, document.get(primaryKey)), new Document("$set", document), new UpdateOptions().upsert(true));
				modifiedCount = updateManyResult.getModifiedCount() + modifiedCount;
			}
			long endTime = new Date().getTime();
			long insCount = collection.count();
			logger.debug("文档更新影响条数：{}", modifiedCount);
			ret = "update num:[ " + modifiedCount + " ]  " + " insert num:[ " + (insCount-preCount) + " ]  spend time:[ " + (endTime-startTime) + "ms ]";
		}
		return ret;
	}
 
	
	/**
	 * 批量删除文档
	 * 
	 * @param table
	 * @param bson Filters.eq("_id", new ArrayList(new ObjectId("5de156a2dbcf3529a36724bc"), new ObjectId("5de156a2dbcf3529a36724bc"))))
	 * @return
	 * @throws Exception
	 */
	public String deleteMany(String table, Bson bson) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		DeleteResult deleteManyResult = collection.deleteMany(bson);
		long deletedCount = deleteManyResult.getDeletedCount();
 
		if (deletedCount > 0) {
			logger.debug("文档删除成功，影响条数：{}，db：{}，table：{}，doc：{} ", deletedCount, DB.getName(), table, bson);
			return "Delete Num:[ " + deletedCount + " ]";
		} else {
			logger.debug("文档删除失败，影响条数：{}，db：{}，table：{}，doc：{} ", 0, DB.getName(), table, bson);
			return "Delete Num:[ 0 ]";
		}
	}
 
	
	/**
	 * 删除单条文档
	 * 
	 * @param table
	 * @param bson Filters.eq("_id", new ObjectId("5de156a2dbcf3529a36724bc")))
	 * @return
	 * @throws Exception
	 */
	public boolean deleteOne(String table, Bson bson) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		DeleteResult deleteOneResult = collection.deleteOne(bson);
		long deletedCount = deleteOneResult.getDeletedCount();
		System.out.println("删除的数量: " + deletedCount);
		if (deletedCount == 1) {
			logger.debug("文档删除成功，影响条数：{}，db：{}，table：{}，doc：{} ", deletedCount, DB.getName(), table, bson);
			return true;
		} else {
			logger.debug("文档删除失败，影响条数：{}，db：{}，table：{}，doc：{} ", 0, DB.getName(), table, bson);
			return false;
		}
	}
 
	
	/**
	 * 修改文档(文档不存在时, insert)
	 * 
	 * @param table
	 * @param whereDoc
	 * @param updateDoc
	 * @return
	 * @throws Exception
	 */
	public String updateManny(String table, Document whereDoc, Document updateDoc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		UpdateResult updateManyResult = collection.updateMany(whereDoc, new Document("$set", updateDoc), new UpdateOptions().upsert(true));
		long modifiedCount = updateManyResult.getModifiedCount();
		System.out.println("修改的数量: " + modifiedCount);
 
		if (modifiedCount > 0) {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", modifiedCount, DB.getName(), table, whereDoc.toJson(), updateDoc.toJson());
			return "Update Num:[ " + modifiedCount + " ]";
		} else {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 0, DB.getName(), table, whereDoc.toJson(), updateDoc.toJson());
			return "Update Num:[ 0 ]";
		}
	}
	
	/**
	 * 修改文档(文档不存在时, insert)
	 * 
	 * @param table
	 * @param bson
	 * @param updateDoc
	 * @return
	 * @throws Exception
	 */
	public String updateManny(String table, Bson bson, Document updateDoc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		UpdateResult updateManyResult = collection.updateMany(bson, new Document("$set", updateDoc), new UpdateOptions().upsert(true));
		long modifiedCount = updateManyResult.getModifiedCount();
		System.out.println("修改的数量: " + modifiedCount);
 
		if (modifiedCount > 0) {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", modifiedCount, DB.getName(), table, bson, updateDoc.toJson());
			return "Update Num:[ " + modifiedCount + " ]";
		} else {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 0, DB.getName(), table, bson, updateDoc.toJson());
			return "Update Num:[ 0 ]";
		}
	}
 
	
	/**
	 * 修改单条文档(文档不存在时, insert)
	 * 
	 * @param table
	 * @param whereDoc
	 * @param updateDoc
	 * @return
	 * @throws Exception
	 */
	public boolean updateOne(String table, Document whereDoc, Document updateDoc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		UpdateResult updateOneResult = collection.updateOne(whereDoc, new Document("$set", updateDoc), new UpdateOptions().upsert(true));
		long modifiedCount = updateOneResult.getModifiedCount();
		System.out.println("修改的数量: " + modifiedCount);
		if (modifiedCount == 1) {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 1, DB.getName(), table, whereDoc.toJson(), updateDoc.toJson());
			return true;
		} else {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 0, DB.getName(), table, whereDoc.toJson(), updateDoc.toJson());
			return false;
		}
	}
	
	
	/**
	 * 修改单条文档(文档不存在时, insert)
	 * 
	 * @param table
	 * @param bson
	 * @param updateDoc
	 * @return
	 * @throws Exception
	 */
	public boolean updateOne(String table, Bson bson, Document updateDoc) throws Exception {
		MongoCollection<Document> collection = DB.getCollection(table);
		UpdateResult updateOneResult = collection.updateOne(bson, new Document("$set", updateDoc), new UpdateOptions().upsert(true));
		long modifiedCount = updateOneResult.getModifiedCount();
		System.out.println("修改的数量: " + modifiedCount);
		if (modifiedCount == 1) {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 1, DB.getName(), table, bson, updateDoc.toJson());
			return true;
		} else {
			logger.debug("文档更新成功，影响条数：{}，db：{}，table：{}，whereDoc：{}，updateDoc：{} ", 0, DB.getName(), table, bson, updateDoc.toJson());
			return false;
		}
	}
 
	
	/**
	 * 创建集合
	 * 
	 * @param table
	 * @throws Exception
	 */
	public void createCol(String table) throws Exception {
		DB.createCollection(table);
		logger.debug("集合创建成功，db：{}，table：{}", DB.getName(), table);
	}
 
	
	/**
	 * 删除集合
	 * 
	 * @param table
	 * @throws Exception
	 */
	public void dropCol(String table) throws Exception {
		DB.getCollection(table).drop();
		logger.debug("集合删除成功，db：{}，table：{}", DB.getName(), table);
 
	}
}
