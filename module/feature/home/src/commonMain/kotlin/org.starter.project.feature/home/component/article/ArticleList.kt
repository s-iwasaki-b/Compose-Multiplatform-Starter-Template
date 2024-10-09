package org.starter.project.feature.home.component.article

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import app.cash.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article

fun LazyListScope.articleList(
    articlesPagingItems: LazyPagingItems<Article>,
) {
    items(
        count = articlesPagingItems.itemCount,
        key = { index -> "article_list_items_$index" }
    ) { index ->
        val article = articlesPagingItems[index] ?: return@items
        Text(text = article.title)
    }
}
