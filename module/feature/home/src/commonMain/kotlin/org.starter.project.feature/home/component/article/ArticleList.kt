package org.starter.project.feature.home.component.article

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        ArticleListItem(article = article)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
