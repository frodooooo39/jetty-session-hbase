/*
 *
 * This module intended to be used for session replication of Jetty via HBase
 * and later will be cached via Ehcache
 *
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.smartitengineering.jetty.session.replication.impl.hbase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.DomainIdInstanceProvider;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import com.smartitengineering.dao.impl.hbase.spi.LockAttainer;
import com.smartitengineering.dao.impl.hbase.spi.LockType;
import com.smartitengineering.dao.impl.hbase.spi.MergeService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.DiffBasedMergeService;
import com.smartitengineering.dao.impl.hbase.spi.impl.LockAttainerImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.MixedExecutorServiceImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.guice.GenericBaseConfigProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.guice.GenericFilterConfigsProvider;
import com.smartitengineering.jetty.session.replication.SessionData;
import com.smartitengineering.jetty.session.replication.SessionId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author imyousuf
 */
public class HBaseImplModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AsyncExecutorService.class).to(MixedExecutorServiceImpl.class).in(Singleton.class);
    bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(100));
    bind(Long.class).annotatedWith(Names.named("waitTime")).toInstance(5l);
    bind(TimeUnit.class).annotatedWith(Names.named("unit")).toInstance(TimeUnit.SECONDS);
    bind(Boolean.class).annotatedWith(Names.named("mergeEnabled")).toInstance(Boolean.TRUE);
    bind(DomainIdInstanceProvider.class).toProvider(Providers.<DomainIdInstanceProvider>of(null));
    bind(LockType.class).toInstance(LockType.PESSIMISTIC);
    bind(new TypeLiteral<Class<String>>() {
    }).toInstance(String.class);


    bind(new TypeLiteral<ObjectRowConverter<SessionData>>() {
    }).to(SessionDataObjectConverter.class).in(Singleton.class);
    bind(CommonReadDao.class).annotatedWith(Names.named("dataReader")).to(new TypeLiteral<CommonReadDao<SessionData, String>>() {
    });
    bind(CommonReadDao.class).annotatedWith(Names.named("idReader")).to(new TypeLiteral<CommonReadDao<SessionId, String>>() {
    });
    bind(CommonWriteDao.class).annotatedWith(Names.named("dataWriter")).to(new TypeLiteral<CommonWriteDao<SessionData>>() {
    });
    bind(CommonWriteDao.class).annotatedWith(Names.named("idWriter")).to(new TypeLiteral<CommonWriteDao<SessionId>>() {
    });
    bind(new TypeLiteral<CommonReadDao<SessionData, String>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionData, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<SessionData>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionData, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionData, String>>() {
    }).to(new TypeLiteral<CommonDao<SessionData, String>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<SessionData, String>> typeLiteral = new TypeLiteral<SchemaInfoProviderImpl<SessionData, String>>() {
    };
    bind(new TypeLiteral<MergeService<SessionData, String>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<SessionData, String>>() {
    });
    bind(new TypeLiteral<LockAttainer<SessionData, String>>() {
    }).to(new TypeLiteral<LockAttainerImpl<SessionData, String>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProvider<SessionData, String>>() {
    }).to(typeLiteral).in(Singleton.class);
    bind(new TypeLiteral<FilterConfigs<SessionData>>() {
    }).toProvider(new GenericFilterConfigsProvider<SessionData>(
        "com/smartitengineering/jetty/session/replication/impl/hbase/SessionDataFilterConfigs.json")).in(
        Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<SessionData>>() {
    }).toProvider(new GenericBaseConfigProvider<SessionData>(
        "com/smartitengineering/jetty/session/replication/impl/hbase/SessionDataSchemaBaseConfig.json")).in(
        Scopes.SINGLETON);

    bind(new TypeLiteral<ObjectRowConverter<SessionId>>() {
    }).to(SessionIdObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<SessionId, String>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<SessionId>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<SessionId, String>>() {
    }).to(new TypeLiteral<CommonDao<SessionId, String>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<SessionId, String>> aTypeLiteral = new TypeLiteral<SchemaInfoProviderImpl<SessionId, String>>() {
    };
    bind(new TypeLiteral<MergeService<SessionId, String>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<SessionId, String>>() {
    });
    bind(new TypeLiteral<LockAttainer<SessionId, String>>() {
    }).to(new TypeLiteral<LockAttainerImpl<SessionId, String>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProvider<SessionId, String>>() {
    }).to(aTypeLiteral).in(Singleton.class);
    bind(new TypeLiteral<FilterConfigs<SessionId>>() {
    }).toProvider(new GenericFilterConfigsProvider<SessionId>(
        "com/smartitengineering/jetty/session/replication/impl/hbase/SessionIdFilterConfigs.json")).in(
        Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<SessionId>>() {
    }).toProvider(new GenericBaseConfigProvider<SessionId>(
        "com/smartitengineering/jetty/session/replication/impl/hbase/SessionIdSchemaBaseConfig.json")).in(
        Scopes.SINGLETON);
  }
}