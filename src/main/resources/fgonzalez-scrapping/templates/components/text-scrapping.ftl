<style>
	${content.cssCode!}
</style>
<div class="${content.cssClass!}">
	[#assign html = model.getScrapping()!]
	[#if html?has_content]	
		<p>${html!""}</p>
	[#else]
		[#if cmsfn.editMode]
			<p>Error: Path not found</p>
		[/#if]
	[/#if]
</div>