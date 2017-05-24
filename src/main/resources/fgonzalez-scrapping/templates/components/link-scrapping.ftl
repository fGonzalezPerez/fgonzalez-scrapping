<style>
	${content.cssCode!}
</style>
<div class="${content.cssClass!}">
	[#assign html = model.getScrapping()!]
	[#if html?has_content]	
		<a href="${html!""}">${html!""}</a>
	[#else]
		[#if cmsfn.editMode]
			<p>Error: Path not found</p>
		[/#if]
	[/#if]
</div>