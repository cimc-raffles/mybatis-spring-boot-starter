package cimc.raffles.mybatis.provider;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.core.ResolvableType;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;

import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

//@lombok.extern.slf4j.Slf4j
//public class CustomSqlProvider implements ProviderMethodResolver
public class CustomSqlProvider
{
	public static String select( ProviderContext context)
	{
		return select( context , new SQL().SELECT("*")) ;
	}
	
	public static String count( ProviderContext context)
	{
		return select( context , new SQL().SELECT("count(*)")) ;
	}
	
	
	private static String select( ProviderContext context , SQL selectSql) 
	{
		java.lang.reflect.Type [] interfaces = context.getMapperType().getGenericInterfaces() ;
		
		if( null==interfaces || 1>interfaces.length)
			;
		
		java.lang.reflect.Type type = interfaces[0] ;
		
		//TODO: super interface class is (or not) BaseMapper.class
//		Class<?> superInterfaceClass = ResolvableType.forType( type).resolve() ;
//		log.info( "{}",superInterfaceClass.equals( BaseMapper.class));
		
		Method method = context.getMapperMethod() ;
		Class<?> entityClass = ResolvableType.forType( type).resolveGenerics()[0];
		
		Parameter parameters [] = method.getParameters() ;
		
		String methodName = method.getName() ;
		
		PartTree tree = new PartTree( methodName, entityClass) ;
		
		List<Part> parts = tree.get().flatMap( orPart -> orPart.get()).collect(Collectors.toList());
		
		TableInfo tableInfo = TableInfoHelper.getTableInfo( entityClass);
		
		
		List<String> conditions = parts2Sql( parts,parameters,entityClass) ;
		
		SQL sql = selectSql.FROM( tableInfo.getTableName());

		for( String x : conditions)
			sql = sql.WHERE( x) ;
			
		return  String.format("<script>%s</script>",  sql.toString()) ;
	}


	private static List<String> parts2Sql( List<Part> parts, Parameter[] parameters , Class<?> entityClass)
	{
		Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap( entityClass) ;
		
		int parameterCursor = 0 ;
		
		//remove pageable patameter ;
		
		Class<?> parameterClass = ResolvableType.forType(  parameters[parameterCursor].getType()).resolve() ;
		
		boolean b = IPage.class.isAssignableFrom(parameterClass) ;
		
		if(b)
			parameterCursor += 1 ;
		
		List<String> resultList = new ArrayList<>() ;
		
		for( int index =0 ; index<parts.size(); ++index)
		{
			Part part = parts.get(index) ;
			
			String columnName = getColumnName( part, columnMap) ;
			
			String parameterName = parameters[parameterCursor].getName() ;
			
			String result = null ;
			
			switch ( part.getType()) 
			{
				case SIMPLE_PROPERTY:
					result = String.format( "%s = #{%s}", columnName, parameterName);
					parameterCursor += 1 ;
					break;
				case NEGATING_SIMPLE_PROPERTY:
					result = String.format( "%s <> %s", columnName, parameterName );
					parameterCursor += 1 ;
					break;
				case BETWEEN:
					result = String.format( "%s between %s and %s", columnName, parameterName, parameters[parameterCursor+1].getName()) ;
					parameterCursor += 2 ;
					break;
					
				case LESS_THAN:
					result = String.format( "%s < %s", columnName, parameterName );
					parameterCursor += 1 ;
					break;
					
				case LESS_THAN_EQUAL:
					result = String.format( "%s <= %s", columnName, parameterName );
					parameterCursor += 1 ;
					break;
					
				case GREATER_THAN:
					result = String.format( "%s > %s", columnName, parameterName );
					parameterCursor += 1 ;
					break;
					
				case GREATER_THAN_EQUAL:
					result = String.format( "%s >= %s", columnName, parameterName );
					parameterCursor += 1 ;
					break;
					
				case IS_NULL:
					result = String.format( "%s IS NULL", columnName);
					break;
					
				case IS_NOT_NULL:
					result = String.format( "%s IS NOT NULL", columnName);
					break;
					
				case LIKE:
					String like1 = "<if test='%s != null'>" ;
					String like2 = "<bind name='%sLike' value=\"'%%' + _parameter.name + '%%'\" />" ;
					String like3 = "%s LIKE #{%sLike}" ;
					String like4 = "</if>" ;
					result = String.format( like1+like2+like3+like4, parameterName,parameterName,columnName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				case NOT_LIKE:
					String notlike1 = "<if test='%s != null'>" ;
					String notlike2 = "<bind name='%sLike' value=\"'%%' + _parameter.name + '%%'\" />" ;
					String notlike3 = "%s NOT LIKE #{%sLike}" ;
					String notlike4 = "</if>" ;
					result = String.format( notlike1+notlike2+notlike3+notlike4, parameterName,parameterName,columnName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				case STARTING_WITH:
					String startswith1 = "<if test='%s != null'>" ;
					String startswith2 = "<bind name='%sLike' value=\"_parameter.name + '%%'\" />" ;
					String startswith3 = "%s LIKE #{%sLike}" ;
					String startswith4 = "</if>" ;
					result = String.format( startswith1+startswith2+startswith3+startswith4, parameterName,parameterName,columnName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				case ENDING_WITH:
					String endswith1 = "<if test='%s != null'>" ;
					String endswith2 = "<bind name='%sLike' value=\"'%%' + _parameter.name\" />" ;
					String endswith3 = "%s LIKE #{%sLike}" ;
					String endswith4 = "</if>" ;
					result = String.format( endswith1+endswith2+endswith3+endswith4, parameterName,parameterName,columnName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				case IN:
					Class<?> inType = parameters[parameterCursor].getType();
					
					String inCollectionName = parameters.length<2 ? 
													( inType.isArray() ? "array" : "collection") :
													parameterName	
					;
					
					String in1 = "%s in " ;
					String in2 = "<foreach collection=\"%s\" item=\"%sItem\" index=\"index\" open=\"(\" close=\")\" separator=\",\"> " ;
					String in3 = "#{%sItem}" ;
					String in4 = "</foreach>" ;
					
					result = String.format( in1+in2+in3+in4, columnName, inCollectionName , parameterName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				case NOT_IN:
					Class<?> notinType = parameters[parameterCursor].getType();
					
					String notinCollectionName = parameters.length<2 ? 
													( notinType.isArray() ? "array" : "collection") :
													parameterName	
					;
					
					String notin1 = "%s in " ;
					String notin2 = "<foreach collection=\"%s\" item=\"%sItem\" index=\"index\" open=\"(\" close=\")\" separator=\",\"> " ;
					String notin3 = "#{%sItem}" ;
					String notin4 = "</foreach>" ;
					
					result = String.format( notin1+notin2+notin3+notin4, columnName,notinCollectionName,parameterName,parameterName) ;
					parameterCursor += 1 ;
					break;
					
				default:
					break;
			}
			
			if( ! StringUtils.isBlank( result))
				resultList.add( result);
		}
		
		return resultList ;
	}
	
	
	private static String getColumnName( Part part , Map<String, ColumnCache> columnMap ) 
	{
		PropertyPath property = part.getProperty() ;
		
		String columnName = property.getSegment() ;
		
		if( null != columnMap.get( columnName))
			columnName = columnMap.get( columnName).getColumnSelect() ;
		
		if( null != columnMap.get( columnName.toUpperCase()))
			columnName = columnMap.get( columnName.toUpperCase()).getColumnSelect() ;
		
		if( null !=  columnMap.get( columnName.toLowerCase()))
			columnName = columnMap.get( columnName.toLowerCase()).getColumnSelect() ;
		
		return columnName ;
	}
	

}
