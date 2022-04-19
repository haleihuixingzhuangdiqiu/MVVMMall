package com.zwb.lib_common.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.zwb.lib_base.mvvm.v.BaseFragment
import com.zwb.lib_base.mvvm.vm.BaseViewModel
import com.zwb.lib_base.net.State
import com.zwb.lib_base.net.StateType
import com.zwb.lib_base.utils.network.NetworkStateClient

/**
 * T 数据类型
 */
abstract class BaseListFragment<T, VB : ViewBinding, VM : BaseViewModel> : BaseFragment<VB, VM>() {
    private lateinit var mRefreshLy: SmartRefreshLayout
    private lateinit var mAdapter: BaseQuickAdapter<T, BaseViewHolder>
    private var mRecyclerListener: RecyclerListener? = null
    private lateinit var mCommRecycler: RecyclerView
    private var mCurrentPage = 1
    private var mPageSize = 15
    private var mHasCheckNet = true


    fun init(
        adapter: BaseQuickAdapter<T, BaseViewHolder>,
        view: RecyclerView,
        refreshLayout: SmartRefreshLayout,
        recyclerListener: RecyclerListener?
    ) {
        mAdapter = adapter
        mCommRecycler = view
        mRefreshLy = refreshLayout
        mRecyclerListener = recyclerListener
        initListener()
        setDefaultLoad(mRefreshLy, LOAD_KEY)
    }
    fun resetAdapter(adapter: BaseQuickAdapter<T, BaseViewHolder>){
        mAdapter = adapter
        initListener()
    }
    private fun initListener(){
        //加载更多
        mAdapter.setOnLoadMoreListener({
            mCurrentPage++
            mRecyclerListener?.loadListData(ACTION_MORE, mPageSize, mCurrentPage)
        }, mCommRecycler)
        //下拉刷新
        mRefreshLy.setOnRefreshListener {
            mRefreshLy.finishRefresh()
            mCurrentPage = 1
            mRecyclerListener?.loadListData(ACTION_REFRESH, mPageSize, mCurrentPage)
        }
//        mAdapter.
    }
    override fun initObserve() {
    }

    override fun initRequestData() {
        if (mHasCheckNet && !NetworkStateClient.isConnected()) {
            mViewModel.loadState.value = State(StateType.NETWORK_ERROR, BaseListActivity.LOAD_KEY)
            return
        }
        mRecyclerListener?.loadListData(BaseListActivity.ACTION_DEFAULT, mPageSize, 1.also { mCurrentPage = it })
    }


    fun loadCompleted(action: Int, list: List<T>? =null, msg: String?="") {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if(action == ACTION_REFRESH){
            mRefreshLy.finishRefresh()
        }

        if(list.isNullOrEmpty()){
            if (action != ACTION_MORE) {
                mAdapter.data.clear()
            }
            mAdapter.loadMoreEnd()
        }else{
            if (action == ACTION_MORE) {
                //加载更多
                mAdapter.addData(list)
            } else {
                //首次加载、下拉刷新
                mAdapter.data.clear()
                mAdapter.addData(list)
            }
            if (list.size < mPageSize) {
                //加载的数据少于一页时
                mAdapter.loadMoreEnd()
            }else{
                mAdapter.loadMoreComplete()
            }
        }
        // 首次加载 或者 下拉刷新
        if(action == ACTION_DEFAULT || action == ACTION_REFRESH){
            if (mAdapter.itemCount == 0) {
                mViewModel.loadState.value = State(StateType.EMPTY, LOAD_KEY)
            } else {
                mViewModel.loadState.value = State(StateType.SUCCESS, LOAD_KEY)
            }
        }
    }

    fun setPageSize(pageSize: Int) {
        mPageSize = pageSize
    }

    fun setHasCheckNet(hsaCheckNet: Boolean) {
        mHasCheckNet = hsaCheckNet
    }

    fun setEnableLoadMore(enabled: Boolean) {
        mAdapter.setEnableLoadMore(enabled)
    }

    fun setRefreshEnable(enabled: Boolean) {
        mRefreshLy.setEnableRefresh(enabled)
    }

    interface RecyclerListener {
        /**
         * 分页加载数据
         * @param action 当前加载的动作
         * @param pageSize 每页数
         * @param page 第几页
         */
        fun loadListData(action: Int, pageSize: Int, page: Int)
    }

    companion object {
        const val LOAD_KEY = "load_fragment_list"
        const val ACTION_REFRESH = 1//刷新
        const val ACTION_MORE = 2//加载更多
        const val ACTION_DEFAULT = 3//首次加载
    }
}