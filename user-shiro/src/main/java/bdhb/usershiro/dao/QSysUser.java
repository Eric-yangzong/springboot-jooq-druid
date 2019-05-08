package bdhb.usershiro.dao;

import org.jooq.Schema;

import com.bdhanbang.base.jooq.IEntity;
import com.bdhanbang.base.jooq.ISchemaSwitch;
import com.generator.tables.SysUser;

public class QSysUser extends SysUser implements ISchemaSwitch, IEntity {

	private static final long serialVersionUID = 1L;

	public static String idName = "user_id";

	Schema schema;

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	@Override
	public String getEntityIdName() {
		return idName;
	}

}