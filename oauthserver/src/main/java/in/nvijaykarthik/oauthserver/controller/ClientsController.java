package in.nvijaykarthik.oauthserver.controller;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import in.nvijaykarthik.oauthserver.config.AuthorityPropertyEditor;
import in.nvijaykarthik.oauthserver.config.SplitCollectionEditor;

@Controller
@RequestMapping("clients")
public class ClientsController {

	@Autowired
	private JdbcClientDetailsService clientsDetailsService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Collection.class, new SplitCollectionEditor(Set.class, ","));
		binder.registerCustomEditor(GrantedAuthority.class, new AuthorityPropertyEditor());
	}

	@RequestMapping(value = "/form", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_OAUTH_ADMIN')")
	public String showEditForm(@RequestParam(value = "client", required = false) String clientId, Model model) {

		ClientDetails clientDetails;
		if (clientId != null) {
			clientDetails = clientsDetailsService.loadClientByClientId(clientId);
		} else {
			clientDetails = new BaseClientDetails();
		}

		model.addAttribute("clientDetails", clientDetails);
		return "form";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_OAUTH_ADMIN')")
	public String editClient(@ModelAttribute BaseClientDetails clientDetails,
			@RequestParam(value = "newClient", required = false) String newClient) {
		
		if (newClient == null) {
			clientsDetailsService.updateClientDetails(clientDetails);
		} else {
			clientsDetailsService.addClientDetails(clientDetails);
		}

		if (!clientDetails.getClientSecret().isEmpty()) {
			clientsDetailsService.updateClientSecret(clientDetails.getClientId(), clientDetails.getClientSecret());
		}
		return "redirect:/";
	}

	@RequestMapping(value = "{client}/delete", method = RequestMethod.GET)
	public String deleteClient(@PathVariable("client") String id) {
		clientsDetailsService.removeClientDetails(id);
		return "redirect:/";
	}
}
