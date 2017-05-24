<div class="${content.cssClass!}" style="${content.cssCode!}">
	[#assign components = model.getScrappingList()!]
	[#if components?has_content]
		[#list components as component]
			<p>${component!}</p>
		[/#list]
	[#else]
		[#if cmsfn.editMode]
			<p>Error: Path not found</p>
		[/#if]
	[/#if]
</div>