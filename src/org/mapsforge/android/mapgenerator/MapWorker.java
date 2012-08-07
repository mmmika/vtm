/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.mapgenerator;

import org.mapsforge.android.IMapRenderer;
import org.mapsforge.android.MapView;
import org.mapsforge.android.utils.PausableThread;

/**
 * A MapWorker uses a {@link IMapGenerator} to generate map tiles. It runs in a separate thread to avoid blocking the UI
 * thread.
 */
public class MapWorker extends PausableThread {
	private final String THREAD_NAME;
	private final JobQueue mJobQueue;
	private final IMapGenerator mMapGenerator;
	private final IMapRenderer mMapRenderer;

	/**
	 * @param id
	 *            thread id
	 * @param mapView
	 *            the MapView for which this MapWorker generates map tiles.
	 * @param mapGenerator
	 *            ...
	 * @param mapRenderer
	 *            ...
	 */
	public MapWorker(int id, MapView mapView, IMapGenerator mapGenerator,
			IMapRenderer mapRenderer) {
		super();
		mJobQueue = mapView.getJobQueue();
		mMapGenerator = mapGenerator;
		mMapRenderer = mapRenderer;

		THREAD_NAME = "MapWorker" + id;
	}

	public IMapGenerator getMapGenerator() {
		return mMapGenerator;
	}

	@Override
	protected void afterRun() {
		// empty
	}

	@Override
	protected void doWork() {
		MapGeneratorJob mapGeneratorJob = mJobQueue.poll();

		if (mMapGenerator == null || mapGeneratorJob == null)
			return;
		// Log.d(THREAD_NAME, "processing: " + mapGeneratorJob.tile);

		boolean success = mMapGenerator.executeJob(mapGeneratorJob);

		if (!isInterrupted() && success) {
			mMapRenderer.passTile(mapGeneratorJob);
		}
	}

	@Override
	protected String getThreadName() {
		return THREAD_NAME;
	}

	@Override
	protected int getThreadPriority() {
		// return (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2;
		return Thread.MIN_PRIORITY;
	}

	@Override
	protected boolean hasWork() {
		return !mJobQueue.isEmpty() && mMapRenderer.processedTile();
	}
}
