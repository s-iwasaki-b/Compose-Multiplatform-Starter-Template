package org.starter.project.feature.user.component.article

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article

internal fun LazyListScope.userArticleList(
    articlesPagingItems: LazyPagingItems<Article>,
) {
    items(
        count = articlesPagingItems.itemCount,
        key = { index -> "user_article_list_items_$index" }
    ) { index ->
        val article = articlesPagingItems[index] ?: return@items
        UserArticleListItem(article = article)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
