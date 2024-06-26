/*
 * Copyright (c) 2019, Trevor <https://github.com/Trevor159>
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

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.definitions.SpotAnimDefinition;
import net.runelite.cache.definitions.loaders.SpotAnimLoader;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.ArchiveFiles;
import net.runelite.cache.fs.FSFile;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Storage;
import net.runelite.cache.fs.Store;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Slf4j
public class SpotanimDumperTest
{
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Rule
	public TemporaryFolder folder = StoreLocation.getTemporaryFolder();

	@Test
	public void test() throws IOException
	{
		File dumpDir = new File(System.getProperty("user.home") + "\\IdeaProjects\\pkhonor-cache-updater\\new_cache\\osrs\\cache\\export\\gfx");
		int count = 0;

		if (!dumpDir.exists()) {
			dumpDir.mkdirs();
		}
		try (Store store = new Store(StoreLocation.LOCATION))
		{
			store.load();

			Storage storage = store.getStorage();
			Index index = store.getIndex(IndexType.CONFIGS);
			Archive archive = index.getArchive(ConfigType.SPOTANIM.getId());

			byte[] archiveData = storage.loadArchive(archive);
			ArchiveFiles files = archive.getFiles(archiveData);

			SpotAnimLoader loader = new SpotAnimLoader();
			String jsonOutput = "[";
			for (FSFile file : files.getFiles())
			{
				byte[] b = file.getContents();

				SpotAnimDefinition def = loader.load(file.getFileId(), b);
				jsonOutput += gson.toJson(def) + ",";

				if (def != null)
				{
					//Files.asCharSink(new File(dumpDir, file.getFileId() + ".json"), Charset.defaultCharset()).write(gson.toJson(def));
					++count;
				}
			}
			jsonOutput = jsonOutput.substring(0, jsonOutput.length() - 1);
			jsonOutput += "]";
			Files.asCharSink(new File(dumpDir, "GFXDump" + ".json"), Charset.defaultCharset()).write(jsonOutput);
		}

		log.info("Dumped {} spotanims to {}", count, dumpDir);
	}
}
