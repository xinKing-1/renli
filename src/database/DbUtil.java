package database;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DbUtil {

    /**
     * 添加(要求返回添加的主键值)
     * @param conn 数据库连接
     * @param sql sql语句
     * @param params 参数集合
     * @return 添加结果
     * <br>成功返回{success:true,msg:"",extra:21,effects:1} extra返回添加的记录的主键
     * <br>失败返回{success:false,msg:"数据库操作错误",extra:0,effects:0}
     */
    public static DaoUpdateResult insert(Connection conn, String sql, Object []params){
        DaoUpdateResult result = new DaoUpdateResult();
        QueryRunner qr = new QueryRunner();
        try {
            result.success = true;
            long id = qr.insert(conn,sql,new ScalarHandler<>(),params);
            result.extra = id;
        } catch (SQLException e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "数据库操作错误";
        }
        return result;
    }

    public static DaoUpdateResult insertBatch(Connection conn, String sql, Object [][]params){
        DaoUpdateResult result = new DaoUpdateResult();
        QueryRunner qr = new QueryRunner();
        try {
            List<Object[]> res = qr.insertBatch(conn,sql,new ArrayListHandler(),params);
            int[] extra = new int[res.size()];
            for(int i=0; i<res.size(); i++){
                long id = (long)res.get(i)[0];
                extra[i] = (int)id;
            }
            result.extra = extra;
            result.success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "数据库操作错误";
        }
        return result;
    }

    /**
     * 添加/删除/修改
     * @param conn 数据库连接
     * @param sql sql语句
     * @param params 参数集合
     * @return 更新结果
     */
    public static DaoUpdateResult update(Connection conn, String sql, Object []params){
        DaoUpdateResult result = new DaoUpdateResult();
        QueryRunner qr = new QueryRunner();
        try {
            result.effects = qr.update(conn,sql,params);
            result.success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "数据库操作错误";
        }
        return result;
    }

    public static DaoUpdateResult batch(Connection conn, String sql, Object [][]params){
        DaoUpdateResult result = new DaoUpdateResult();
        QueryRunner qr = new QueryRunner();
        try {
            int []res = qr.batch(conn,sql,params);
            int effects = 0;
            for(int n:res){
                effects += n;
            }
            result.effects = effects;
            result.success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "数据库操作错误";
        }
        return result;
    }

    public static DaoQueryListResult getList(Connection conn, String table, QueryParameter param, Class c){
        String sql1 = String.format("select * from %s where ",table);
        String sql2 = String.format("select count(*) from %s where ",table);

        String condition = param.conditions.toString();
        sql1 += condition;
        sql2 += condition;

        if(param.order.need){
            sql1 += (" order by "+param.order.field);
            sql1 += param.order.direction?" asc":" desc";
        }

        if(param.pagination.need){
            sql1 += String.format(" limit %d,%d",(param.pagination.page-1)*param.pagination.size,param.pagination.size);
        }

        QueryRunner qr = new QueryRunner();
        DaoQueryListResult result = new DaoQueryListResult();
        try {
            Object[] values = param.conditions.extraValues().toArray();
            result.success = true;
            result.rows = qr.query(conn, sql1, new BeanListHandler<>(c), values);
            if(param.pagination.need) {
                result.total = qr.query(conn, sql2, new ScalarHandler<Long>(), values);
            }
        }catch (SQLException e){
            result.success = false;
            result.msg = "数据库操作错误";
            e.printStackTrace();
        }

        return result;
    }

    public static DaoQueryListResult getArray(Connection conn, String table, String ids, Class c){
        String sql = String.format("select * from %s where id in (%s)",table,ids);

        DaoQueryListResult result = new DaoQueryListResult();
        QueryRunner qr = new QueryRunner();
        try {
            result.success = true;
            result.rows = qr.query(conn, sql, new BeanListHandler<>(c));
        }catch (SQLException e){
            result.success = false;
            result.msg = "数据库操作错误";
            e.printStackTrace();
        }

        return result;
    }

    public static DaoQueryResult get(Connection conn, String table, QueryConditions conditions, Class c){
        String sql = String.format("select * from %s where ",table);
        String condition = conditions.toString();
        sql += condition;
        sql += " limit 1";
        DaoQueryResult result = new DaoQueryResult();
        QueryRunner qr = new QueryRunner();
        try {
            List<Object> values = conditions.extraValues();
            result.data = qr.query(conn, sql, new BeanHandler<>(c),values.toArray());
            result.success = true;
        }catch (SQLException e){
            result.success = false;
            result.msg = "数据库操作错误";
            e.printStackTrace();
        }

        return result;
    }

    public static DaoExistResult exist(Connection conn, String table, QueryConditions conditions){
        String sql = String.format("select count(*) as num from %s where ",table);
        String condition = conditions.toString();
        sql += condition;
        sql += " limit 1";

        DaoExistResult result = new DaoExistResult();
        QueryRunner qr = new QueryRunner();
        try {
            List<Object> values = conditions.extraValues();
            long n = qr.query(conn,sql, new ScalarHandler<Long>(),values.toArray());
            result.success = true;
            result.exist = n==1;
        } catch (SQLException e) {
            e.printStackTrace();
            result.success = false;
            result.msg = "数据库操作错误";
        }

        return result;
    }
}
