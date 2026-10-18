package com.dc.android.foundation.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class DcContentStatePolicyTest {
    @Test fun `stale content wins over loading`() = assertEquals(
        DcContentStateBranch.Content,
        resolveDcContentStateBranch(hasContent = true, isLoading = true, hasError = false)
    )

    @Test fun `stale content wins over error`() = assertEquals(
        DcContentStateBranch.Content,
        resolveDcContentStateBranch(hasContent = true, isLoading = false, hasError = true)
    )

    @Test fun `empty error wins over loading`() = assertEquals(
        DcContentStateBranch.Error,
        resolveDcContentStateBranch(hasContent = false, isLoading = true, hasError = true)
    )

    @Test fun `empty loading renders loading`() = assertEquals(
        DcContentStateBranch.Loading,
        resolveDcContentStateBranch(hasContent = false, isLoading = true, hasError = false)
    )

    @Test fun `empty content renders empty`() = assertEquals(
        DcContentStateBranch.Empty,
        resolveDcContentStateBranch(hasContent = false, isLoading = false, hasError = false)
    )
}
