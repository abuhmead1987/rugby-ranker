package com.ricknout.rugbyranker.work

import android.content.Context
import androidx.work.WorkerParameters
import com.ricknout.rugbyranker.api.WorldRugbyService
import com.ricknout.rugbyranker.db.WorldRugbyRankingDao
import com.ricknout.rugbyranker.vo.RankingsType

class WomensWorldRugbyRankingsWorker(
        context: Context,
        workerParams: WorkerParameters,
        worldRugbyService: WorldRugbyService,
        worldRugbyRankingDao: WorldRugbyRankingDao
) : WorldRugbyRankingsWorker(context, workerParams, worldRugbyService, worldRugbyRankingDao, RankingsType.WOMENS) {

    companion object {
        const val UNIQUE_WORK_NAME = "world_rugby_rankings_worker_womens"
    }
}