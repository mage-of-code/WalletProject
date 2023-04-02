package com.example.WalletProject.services;

import com.example.WalletProject.models.DTO.ClientDto;
import com.example.WalletProject.models.DTO.ClientInformationForMainPageDTO;
import com.example.WalletProject.models.DTO.ClientInformationForManageDTO;
import com.example.WalletProject.models.DTO.RegistrationDto;
import com.example.WalletProject.models.Entity.AuthInfo;
import com.example.WalletProject.models.Entity.Client;
import com.example.WalletProject.repositories.AuthInfoRepository;
import com.example.WalletProject.repositories.ClientRepository;
import com.example.WalletProject.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService implements UserDetailsService {
    private final ClientRepository clientRepository;
    private final AuthInfoRepository authInfoRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public ClientService(ClientRepository clientRepository, AuthInfoRepository authInfoRepository, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.authInfoRepository = authInfoRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }


    public List<ClientDto> getAllClients(Integer numberPage) {
        Page<Client> allClients = clientRepository.findAll(PageRequest.of(numberPage, 20));
        return allClients.stream()
                .map(client -> modelMapper.map(client, ClientDto.class))
                .collect(Collectors.toList());
    }


    public ClientInformationForMainPageDTO getClientById(Long id) {
        Client client = finedOrThrow(id);
        return modelMapper.map(client, ClientInformationForMainPageDTO.class);
    }

    //не понятен смыс метода
    public ClientDto getClientByIdForAdmin(Long id) {
        return clientRepository.findById(id)
                .map(client -> modelMapper.map(client, ClientDto.class)
                )
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
    }

    public ClientInformationForManageDTO getClientInformationForManageByClientId(Long id) {
        Client client = finedOrThrow(id);
        return modelMapper.map(client, ClientInformationForManageDTO.class);
    }

    public void createNewClient(RegistrationDto registrationDto) {
        Client client = modelMapper.map(registrationDto, Client.class);
        // set role сразу юзера и всегда юзера
        client.setRole(roleRepository.findById(Roles.USER.id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found")));
        clientRepository.save(client);
        AuthInfo authInfo = new AuthInfo(
                client.getId(),
                registrationDto.getPassword()
        );
        authInfoRepository.save(authInfo);
    }

    //перезанирание информации о клиенте в entity из-за пустых полей dto
    public void updateInformationByClientId(Long id, ClientInformationForMainPageDTO clientInformationForMainPageDTO) {
        Client clientToBeUpdated = finedOrThrow(id);
        Client client = modelMapper.map(clientInformationForMainPageDTO, Client.class);
        client.setId(clientToBeUpdated.getId());
//        client.setFirstname(clientInformationForMainPageDTO.getFirstname());
//        client.setLastname(clientInformationForMainPageDTO.getLastname());
//        client.setPatronymic(clientInformationForMainPageDTO.getPatronymic());
//        client.setDateOfBirth(clientInformationForMainPageDTO.getDateOfBirth());
//        client.setEmail(clientInformationForMainPageDTO.getEmail());
//        client.setPhoneNumber(clientInformationForMainPageDTO.getPhoneNumber());
//        client.setUpdatedAt(LocalDate.now());
        clientRepository.save(client);
    }

    public void updateInformationForManageByClientId(Long id, ClientInformationForManageDTO clientInformationForManageDTO) {
        Client client = finedOrThrow(id);
        client.setFrozen(clientInformationForManageDTO.getFrozen());
        client.setIsVerify(clientInformationForManageDTO.getIsVerify());
        clientRepository.save(client);
    }

    public void deleteClientById(Long id) {
        Client client = finedOrThrow(id);
        client.setDelete(true);
        clientRepository.save(client);
    }

    private Client finedOrThrow(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        if (client.getIsDelete().equals(true)) {
            throw new SecurityException("Client removed");
        }
        return client;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Client not found"));
        AuthInfo authInfo = authInfoRepository.findById(client.getId()).orElseThrow(() -> new EntityNotFoundException("AufInfo not found"));
        return new User(client.getEmail(), authInfo.getPassword(), client.getRoles());
    }

    private enum Roles {
        USER(1), ADMIN(2);
        private Integer id;

        Roles(Integer id) {
            this.id = id;
        }

    }
}