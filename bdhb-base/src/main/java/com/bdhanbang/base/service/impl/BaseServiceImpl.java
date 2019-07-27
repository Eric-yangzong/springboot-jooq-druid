package com.bdhanbang.base.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteWhereStep;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.bdhanbang.base.common.Query;
import com.bdhanbang.base.common.QueryPage;
import com.bdhanbang.base.common.QueryResults;
import com.bdhanbang.base.exception.CurdException;
import com.bdhanbang.base.jooq.GenSchema;
import com.bdhanbang.base.jooq.ISchemaSwitch;
import com.bdhanbang.base.message.ErrorMessage;
import com.bdhanbang.base.service.BaseService;
import com.bdhanbang.base.util.JOOQHelper;

/**
 * @ClassName: BaseServiceImpl
 * @Description: Service的基础实现类
 * @author yangxz
 * @date 2018年7月14日 上午10:51:16
 * 
 * @param <T> Q开头的查询用和tableImpl
 * @param <E> 询用实体
 */
public class BaseServiceImpl<T extends TableImpl<? extends Record>, E extends Serializable>
		implements BaseService<T, E> {

	@Autowired
	protected DSLContext dsl;

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int deleteEntity(String schema, Class<T> clazz, Object id) {

		T tableImpl = createTableImpl(schema, clazz);

		DeleteWhereStep<?> deleteStep = dsl.delete((Table) tableImpl);

		TableField<?, Object> tableIdField = getTableIdField(tableImpl);

		deleteStep.where(tableIdField.eq(id));

		return deleteStep.execute();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int deleteEntitys(String schema, Class<T> clazz, List<Query> queryList) {

		T tableImpl = createTableImpl(schema, clazz);

		DeleteWhereStep<?> deleteStep = dsl.delete((Table) tableImpl);

		Condition delWhere = JOOQHelper.analyzeQuery(tableImpl, queryList);

		deleteStep.where(delWhere);

		return deleteStep.execute();

	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int insertEntity(String schema, Class<T> clazz, E entity) {

		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();
		List<Object> values = getValueList(fields, entity);

		InsertValuesStepN<?> insertStep = dsl.insertInto((Table) tableImpl).columns(fields);

		insertStep.values(values);// 进行赋值工作

		return insertStep.execute();// 执行写入工作

	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int insertEntityBatch(String schema, Class<T> clazz, List<E> entitys) {
		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();
		InsertValuesStepN<?> insertStep = dsl.insertInto((Table) tableImpl).columns(fields);
		for (E entity : entitys) {
			List<Object> values = getValueList(fields, entity);
			insertStep.values(values);// 进行赋值工作
		}
		return insertStep.execute();// 执行写入工作
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int updateEntity(String schema, Class<T> clazz, E entity) {

		T tableImpl = createTableImpl(schema, clazz);

		TableField<?, Object> tableIdField = getTableIdField(tableImpl);
		Object idValue = getFieldIdValue(tableImpl, entity);

		Field<?>[] fields = tableImpl.fields();
		List<Object> values = getValueList(fields, entity);

		TableField<?, Object> tableField = (TableField<?, Object>) fields[0];
		UpdateSetMoreStep<?> updateStep = dsl.update((Table) tableImpl).set(tableField, values.get(0));

		for (int i = 1; i < fields.length; i++) {
			tableField = (TableField<?, Object>) fields[i];
			updateStep = updateStep.set(tableField, values.get(i));
		}

		updateStep.where(tableIdField.eq(idValue));

		return updateStep.execute();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int updateEntitys(String schema, Class<T> clazz, E entity, List<Query> queryList) {

		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();
		List<Object> values = getValueList(fields, entity);

		TableField<?, Object> tableField = (TableField<?, Object>) fields[0];
		UpdateSetMoreStep<?> updateStep = dsl.update((Table) tableImpl).set(tableField, values.get(0));

		for (int i = 0; i < fields.length; i++) {
			tableField = (TableField<?, Object>) fields[i];
			updateStep.set(tableField, values.get(i));
		}

		Condition updateWhere = JOOQHelper.analyzeQuery(tableImpl, queryList);

		updateStep.where(updateWhere);

		return updateStep.execute();

	}

	@Override
	public E getEntity(String schema, Class<T> clazz, Class<E> entityClass, Object id) {

		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();

		SelectJoinStep<Record> selectStep = dsl.select(fields).from(tableImpl);

		TableField<?, Object> tableIdField = getTableIdField(tableImpl);

		selectStep.where(tableIdField.eq(id));

		Integer count = dsl.select(DSL.count()).from(tableImpl).where(tableIdField.eq(id)).fetchOne()
				.into(Integer.class);

		if (count == 0) {
			return null;
		}

		return selectStep.fetchOne().into(entityClass);

	}

	@Override
	public List<E> queryList(String schema, Class<T> clazz, Class<E> entityClass, List<Query> queryList) {

		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();

		SelectJoinStep<Record> selectStep = dsl.select(fields).from(tableImpl);

		// 生成查询条件
		Condition condition = JOOQHelper.analyzeQuery(tableImpl, queryList);

		// 生成where
		if (!Objects.isNull(condition)) {
			selectStep.where(condition);
		}

		List<E> list = selectStep.fetch().into(entityClass);

		return list;
	}

	@Override
	public QueryResults<E> queryPage(String schema, Class<T> clazz, Class<E> entityClass, QueryPage queryPage) {

		T tableImpl = createTableImpl(schema, clazz);

		Field<?>[] fields = tableImpl.fields();

		SelectJoinStep<Record> selectStep = dsl.select(fields).from(tableImpl);

		JOOQHelper.analyzeQuery(tableImpl, selectStep, queryPage);

		int rowCount = dsl.fetchCount(selectStep); // 得到总行数

		JOOQHelper.analyzeOrder(tableImpl, selectStep, queryPage);

		List<E> list = selectStep.fetch().into(entityClass);

		return new QueryResults<E>(list, Long.valueOf(queryPage.getSize()),
				Long.valueOf((queryPage.getPage() - 1) * queryPage.getSize()), Long.valueOf(rowCount));

	}

	/**
	 * @Title: getFieldIdValue
	 * @Description: 得到实体的ID值
	 * @param @param  tableImpl
	 * @param @param  entity
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws:
	 */
	protected Object getFieldIdValue(T tableImpl, E entity) {

		try {

			String idName = tableImpl.getPrimaryKey().getFields().get(0).getName();

			java.lang.reflect.Field fieldVal = entity.getClass().getDeclaredField(JOOQHelper.UnderlineToHump(idName));
			fieldVal.setAccessible(true);

			return fieldVal.get(entity);

		} catch (NoSuchFieldException e) {

			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());

		} catch (SecurityException e) {

			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());

		} catch (IllegalArgumentException e) {

			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());

		} catch (IllegalAccessException e) {

			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());
		}

	}

	/**
	 * @Title: getTableIdField
	 * @Description: 得到table的ID
	 * @param @param  tableImpl
	 * @param @return 设定文件
	 * @return TableField<?,Object> 返回类型
	 * @throws:
	 */
	@SuppressWarnings("unchecked")
	protected TableField<?, Object> getTableIdField(T tableImpl) {
		return (TableField<?, Object>) tableImpl.getPrimaryKey().getFields().get(0);

	}

	/**
	 * @Title: getValueList
	 * @Description: 得到实体的值列表
	 * @param @param  fields
	 * @param @param  entity
	 * @param @return 设定文件
	 * @return List<Object> 返回类型
	 * @throws:
	 */
	protected List<Object> getValueList(Field<?>[] fields, E entity) {
		try {

			List<Object> values = new ArrayList<>();

			for (Field<?> field : fields) {

				java.lang.reflect.Field fieldVal = entity.getClass()
						.getDeclaredField(JOOQHelper.UnderlineToHump(field.getName()));
				fieldVal.setAccessible(true);

				values.add(fieldVal.get(entity));

			}
			return values;
		} catch (Exception e) {
			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());
		}
	}

	/**
	 * @Title: createTableImpl
	 * @Description: 生成jooq的tableImpl
	 * @param @param  schema
	 * @param @param  clazz
	 * @param @return 设定文件
	 * @return T 返回类型
	 * @throws:
	 */
	protected T createTableImpl(String schema, Class<T> clazz) {
		try {
			T tableImpl = clazz.newInstance();

			GenSchema s = new GenSchema(schema);
			((ISchemaSwitch) tableImpl).setSchema(s);

			return tableImpl;
		} catch (Exception e) {
			throw new CurdException(e, ErrorMessage.CURD_ERROR.getStatus(), ErrorMessage.CURD_ERROR.getMessage());
		}

	}

}
