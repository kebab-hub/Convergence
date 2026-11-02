package io.github.kebab_hub.subspace.common.link;

/** Composable link configuration boundary; direct hosts are never followed recursively. */
public interface DirectLinkHost {
    DirectLinkConfig linkConfig();
}
