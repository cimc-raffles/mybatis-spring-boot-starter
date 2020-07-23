package cimc.raffles.mybatis.pagination;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = PageSerializer.class)
public class Pageable<T> extends com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> 
{
	private static final long serialVersionUID = -8040031864691800239L;
}


class PageSerializer<T> extends JsonSerializer<Pageable<T>>
{
	@Override
	public void serialize( Pageable<T> v, JsonGenerator g, SerializerProvider s) throws IOException 
	{
		g.writeStartObject();
		
		g.writeObjectField( "content", v.getRecords());
		g.writeNumberField( "page", -1L + v.getCurrent());
		g.writeNumberField( "size", v.getSize());
		g.writeNumberField( "totalPages", v.getPages());
		g.writeNumberField( "totalElements", v.getTotal());
		g.writeBooleanField( "first", ! v.hasPrevious());
		g.writeBooleanField( "last", ! v.hasNext());
		
		g.writeEndObject();
	}
}
