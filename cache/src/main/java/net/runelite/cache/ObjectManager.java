/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.definitions.exporters.ObjectExporter;
import net.runelite.cache.definitions.loaders.ObjectLoader;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.ArchiveFiles;
import net.runelite.cache.fs.FSFile;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Storage;
import net.runelite.cache.fs.Store;
import net.runelite.cache.util.IDClass;

public class ObjectManager
{
	private final Store store;
	private final Map<Integer, ObjectDefinition> objects = new HashMap<>();

	public ObjectManager(Store store)
	{
		this.store = store;
	}

	private final Gson gson = new GsonBuilder().create();

	public void load() throws IOException
	{

		ObjectLoader loader = new ObjectLoader();

		Storage storage = store.getStorage();
		Index index = store.getIndex(IndexType.CONFIGS);
		Archive archive = index.getArchive(ConfigType.OBJECT.getId());

		byte[] archiveData = storage.loadArchive(archive);
		ArchiveFiles files = archive.getFiles(archiveData);

		for (FSFile f : files.getFiles())
		{
			ObjectDefinition def = loader.load(f.getFileId(), f.getContents());

			objects.put(f.getFileId(), def);
			System.out.println(def.getId() + "/" + 49583);
		}

	}

	public Collection<ObjectDefinition> getObjects()
	{
		return Collections.unmodifiableCollection(objects.values());
	}

	public ObjectDefinition getObject(int id)
	{
		return objects.get(id);
	}

	public void dump(File out) throws IOException
	{
		out.mkdirs();
		String jsonOutput = "[";

		for (ObjectDefinition def : objects.values())
		{
			System.out.println(def.getId() + "/" + 49583);
			ObjectExporter exporter = new ObjectExporter(def);
			jsonOutput += gson.toJson(def) + ",";
			//File targ = new File(out, def.getId() + ".json");
			//exporter.exportTo(targ);

		}
		jsonOutput = jsonOutput.substring(0, jsonOutput.length() - 1);
		jsonOutput += "]";
		Files.asCharSink(new File(out, "ObjectDump" + ".json"), Charset.defaultCharset()).write(jsonOutput);
	}

	public void java(File java) throws IOException
	{
		java.mkdirs();
		try (IDClass ids = IDClass.create(java, "ObjectID");
			IDClass nulls = IDClass.create(java, "NullObjectID"))
		{
			for (ObjectDefinition def : objects.values())
			{
				if ("null".equals(def.getName()))
				{
					nulls.add(def.getName(), def.getId());
				}
				else
				{
					ids.add(def.getName(), def.getId());
				}
			}
		}
	}
}
