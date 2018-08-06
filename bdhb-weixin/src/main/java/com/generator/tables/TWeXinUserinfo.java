/*
 * This file is generated by jOOQ.
*/
package com.generator.tables;


import com.bdhanbang.base.jooq.PostgresJsonBinding;
import com.fasterxml.jackson.databind.JsonNode;
import com.generator.Indexes;
import com.generator.Keys;
import com.generator.Tat0003ModLogin;
import com.generator.tables.records.TWeXinUserinfoRecord;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TWeXinUserinfo extends TableImpl<TWeXinUserinfoRecord> {

    private static final long serialVersionUID = -1241671948;

    /**
     * The reference instance of <code>tat0003_mod_login.t_we_xin_userinfo</code>
     */
    public static final TWeXinUserinfo T_WE_XIN_USERINFO = new TWeXinUserinfo();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TWeXinUserinfoRecord> getRecordType() {
        return TWeXinUserinfoRecord.class;
    }

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.id</code>.
     */
    public final TableField<TWeXinUserinfoRecord, UUID> ID = createField("id", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.open_id</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> OPEN_ID = createField("open_id", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.nick_name</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> NICK_NAME = createField("nick_name", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.gender</code>.
     */
    public final TableField<TWeXinUserinfoRecord, Integer> GENDER = createField("gender", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.language</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> LANGUAGE = createField("language", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.city</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> CITY = createField("city", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.province</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> PROVINCE = createField("province", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.country</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> COUNTRY = createField("country", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.avatar_url</code>.
     */
    public final TableField<TWeXinUserinfoRecord, String> AVATAR_URL = createField("avatar_url", org.jooq.impl.SQLDataType.VARCHAR(500), this, "");

    /**
     * The column <code>tat0003_mod_login.t_we_xin_userinfo.watermark</code>.
     */
    public final TableField<TWeXinUserinfoRecord, JsonNode> WATERMARK = createField("watermark", org.jooq.impl.DefaultDataType.getDefaultDataType("jsonb"), this, "", new PostgresJsonBinding());

    /**
     * Create a <code>tat0003_mod_login.t_we_xin_userinfo</code> table reference
     */
    public TWeXinUserinfo() {
        this(DSL.name("t_we_xin_userinfo"), null);
    }

    /**
     * Create an aliased <code>tat0003_mod_login.t_we_xin_userinfo</code> table reference
     */
    public TWeXinUserinfo(String alias) {
        this(DSL.name(alias), T_WE_XIN_USERINFO);
    }

    /**
     * Create an aliased <code>tat0003_mod_login.t_we_xin_userinfo</code> table reference
     */
    public TWeXinUserinfo(Name alias) {
        this(alias, T_WE_XIN_USERINFO);
    }

    private TWeXinUserinfo(Name alias, Table<TWeXinUserinfoRecord> aliased) {
        this(alias, aliased, null);
    }

    private TWeXinUserinfo(Name alias, Table<TWeXinUserinfoRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Tat0003ModLogin.TAT0003_MOD_LOGIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.T_WE_XIN_USERINFO_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TWeXinUserinfoRecord> getPrimaryKey() {
        return Keys.T_WE_XIN_USERINFO_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TWeXinUserinfoRecord>> getKeys() {
        return Arrays.<UniqueKey<TWeXinUserinfoRecord>>asList(Keys.T_WE_XIN_USERINFO_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TWeXinUserinfo as(String alias) {
        return new TWeXinUserinfo(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TWeXinUserinfo as(Name alias) {
        return new TWeXinUserinfo(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TWeXinUserinfo rename(String name) {
        return new TWeXinUserinfo(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TWeXinUserinfo rename(Name name) {
        return new TWeXinUserinfo(name, null);
    }
}